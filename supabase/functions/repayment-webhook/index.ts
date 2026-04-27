import { corsHeaders } from "../_shared/cors.ts";
import { adminClient, json, toErrorResponse, writeAuditLog } from "../_shared/auth.ts";

const WEBHOOK_SHARED_SECRET = Deno.env.get("PAYMENT_WEBHOOK_SECRET") ?? "";

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: corsHeaders });
  if (req.method !== "POST") return json(405, { ok: false, error: "Method not allowed." });

  try {
    const providedSecret = req.headers.get("x-webhook-secret") ?? "";
    if (!WEBHOOK_SHARED_SECRET || providedSecret !== WEBHOOK_SHARED_SECRET) {
      throw { code: 401, message: "Unauthorized webhook." };
    }

    const body = await req.json();
    const intentId = String(body.paymentIntentId ?? "").trim();
    const providerReference = String(body.providerReference ?? "").trim();
    const settledAmountKes = Number(body.settledAmountKes);
    const settled = Boolean(body.settled);

    if (!intentId) throw { code: 400, message: "Missing paymentIntentId." };
    if (!providerReference) throw { code: 400, message: "Missing providerReference." };
    if (!Number.isFinite(settledAmountKes) || settledAmountKes <= 0) {
      throw { code: 400, message: "Invalid settled amount." };
    }

    const { data: intent, error: intentErr } = await adminClient
      .from("payment_intents")
      .select("id, user_id, loan_id, amount_kes, status")
      .eq("id", intentId)
      .maybeSingle();
    if (intentErr) throw { code: 500, message: intentErr.message };
    if (!intent) throw { code: 404, message: "Payment intent not found." };
    if (intent.status === "SETTLED" || intent.status === "FAILED") {
      return json(200, { ok: true, alreadyProcessed: true, status: intent.status });
    }

    if (!settled) {
      await adminClient
        .from("payment_intents")
        .update({
          status: "FAILED",
          provider_reference: providerReference,
          updated_at: new Date().toISOString(),
        })
        .eq("id", intentId);
      await writeAuditLog(intent.user_id, "loan.repay_webhook_failed", { intentId, providerReference });
      return json(200, { ok: true, settled: false });
    }

    const { data: loan, error: loanErr } = await adminClient
      .from("loans")
      .select("id, outstanding_kes, repaid_amount_kes, status")
      .eq("id", intent.loan_id)
      .eq("user_id", intent.user_id)
      .single();
    if (loanErr) throw { code: 404, message: "Loan not found for settlement." };

    const payment = Math.min(
      settledAmountKes,
      Number(intent.amount_kes),
      Number(loan.outstanding_kes),
    );
    const nextOutstanding = Math.max(0, Number(loan.outstanding_kes) - payment);
    const nextRepaid = Number(loan.repaid_amount_kes ?? 0) + payment;
    const nextStatus = nextOutstanding <= 0.009 ? "PAID" : "ACTIVE";

    const { error: txErr } = await adminClient
      .from("transactions")
      .insert({
        user_id: intent.user_id,
        loan_id: intent.loan_id,
        title: "Loan repayment",
        amount_kes: payment,
        tx_type: "DEBIT",
        status: "COMPLETED",
        source: "MPESA",
      });
    if (txErr) throw { code: 500, message: txErr.message };

    const { error: loanUpdateErr } = await adminClient
      .from("loans")
      .update({
        outstanding_kes: nextOutstanding,
        repaid_amount_kes: nextRepaid,
        status: nextStatus,
      })
      .eq("id", loan.id);
    if (loanUpdateErr) throw { code: 500, message: loanUpdateErr.message };

    await adminClient
      .from("payment_intents")
      .update({
        status: "SETTLED",
        provider_reference: providerReference,
        updated_at: new Date().toISOString(),
      })
      .eq("id", intentId);

    await writeAuditLog(intent.user_id, "loan.repay_webhook_settled", {
      intentId,
      providerReference,
      payment,
      loanId: intent.loan_id,
      status: nextStatus,
    });

    return json(200, {
      ok: true,
      settled: true,
      loan: {
        id: loan.id,
        outstanding_kes: nextOutstanding,
        repaid_amount_kes: nextRepaid,
        status: nextStatus,
      },
    });
  } catch (error) {
    return toErrorResponse(error);
  }
});

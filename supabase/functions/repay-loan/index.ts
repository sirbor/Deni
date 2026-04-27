import { corsHeaders } from "../_shared/cors.ts";
import {
  adminClient,
  clientForJwt,
  json,
  readBearer,
  toErrorResponse,
  writeAuditLog,
} from "../_shared/auth.ts";

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: corsHeaders });
  if (req.method !== "POST") return json(405, { ok: false, error: "Method not allowed." });

  try {
    const jwt = readBearer(req);
    const client = clientForJwt(jwt);
    const { data: authUserData, error: authUserErr } = await client.auth.getUser();
    if (authUserErr || !authUserData.user) throw { code: 401, message: "Invalid auth token." };
    const userId = authUserData.user.id;

    const body = await req.json();
    const idempotencyKey = String(
      req.headers.get("Idempotency-Key") ?? body.idempotencyKey ?? "",
    ).trim();
    const loanId = String(body.loanId ?? "").trim();
    const amountKes = Number(body.amountKes);
    const source = String(body.source ?? "MPESA");
    if (!idempotencyKey) throw { code: 400, message: "Missing idempotency key." };

    if (!loanId) throw { code: 400, message: "Invalid loanId." };
    if (!Number.isFinite(amountKes) || amountKes <= 0) throw { code: 400, message: "Amount must be greater than zero." };

    const { data: prior } = await adminClient
      .from("api_idempotency_keys")
      .select("response_payload")
      .eq("user_id", userId)
      .eq("endpoint", "repay-loan")
      .eq("idempotency_key", idempotencyKey)
      .maybeSingle();
    if (prior?.response_payload) {
      return json(200, prior.response_payload as Record<string, unknown>);
    }

    const { data: loan, error: loanErr } = await client
      .from("loans")
      .select("id, outstanding_kes, repaid_amount_kes, amount_repaid, status")
      .eq("id", loanId)
      .eq("user_id", userId)
      .single();
    if (loanErr) throw { code: 404, message: "Loan not found." };
    if (loan.status === "PAID") throw { code: 400, message: "Loan is already fully paid." };

    const payment = Number(amountKes.toFixed(2));
    const currentOutstanding = Number(loan.outstanding_kes ?? 0);
    const currentRepaid = Number(loan.repaid_amount_kes ?? loan.amount_repaid ?? 0);
    if (payment > currentOutstanding) throw { code: 400, message: "Amount exceeds outstanding balance." };

    const nextOutstanding = Number((currentOutstanding - payment).toFixed(2));
    const nextRepaid = Number((currentRepaid + payment).toFixed(2));
    const nextStatus = nextOutstanding <= 1 ? "PAID" : "ACTIVE";

    const { data: updatedLoan, error: loanUpdateErr } = await adminClient
      .from("loans")
      .update({
        outstanding_kes: nextOutstanding,
        repaid_amount_kes: nextRepaid,
        amount_repaid: nextRepaid,
        status: nextStatus,
        updated_at: new Date().toISOString(),
      })
      .eq("id", loanId)
      .eq("user_id", userId)
      .select("id, outstanding_kes, status, updated_at")
      .single();
    if (loanUpdateErr) throw { code: 500, message: loanUpdateErr.message };

    const { data: txRow, error: txErr } = await adminClient
      .from("transactions")
      .insert({
        user_id: userId,
        loan_id: loanId,
        title: "M-Pesa Repayment",
        amount_kes: payment,
        tx_type: "DEBIT",
        status: nextStatus,
        source,
      })
      .select("id, loan_id, amount_kes, tx_type, status, created_at")
      .single();
    if (txErr) throw { code: 500, message: txErr.message };

    const responsePayload = {
      ok: true,
      loan: updatedLoan,
      transaction: txRow,
    };
    await adminClient
      .from("api_idempotency_keys")
      .upsert(
        {
          user_id: userId,
          endpoint: "repay-loan",
          idempotency_key: idempotencyKey,
          response_payload: responsePayload,
        },
        { onConflict: "user_id,endpoint,idempotency_key" },
      );
    await writeAuditLog(userId, "loan.repay_settled", {
      loanId,
      amountKes: payment,
      nextOutstanding,
      nextStatus,
      transactionId: txRow.id,
      idempotencyKey,
      source,
    });
    return json(200, responsePayload);
  } catch (error) {
    return toErrorResponse(error);
  }
});

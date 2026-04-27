import { corsHeaders } from "../_shared/cors.ts";
import {
  adminClient,
  clientForJwt,
  json,
  readBearer,
  toErrorResponse,
  writeAuditLog,
} from "../_shared/auth.ts";
import { getPricingForTenureDays } from "../_shared/pricing.ts";

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
    const loanLookup = String(body.loanId ?? "").trim();
    const topupAmountKes = Number(body.topupAmountKes);
    const extensionDays = Number(body.extensionDays);
    const topupPurpose = String(body.topupPurpose ?? "Personal").trim() || "Personal";
    const idempotencyKey = String(req.headers.get("Idempotency-Key") ?? body.idempotencyKey ?? "").trim();
    if (!idempotencyKey) throw { code: 400, message: "Missing idempotency key." };
    if (!loanLookup) throw { code: 400, message: "Missing loanId." };
    if (!Number.isFinite(topupAmountKes) || topupAmountKes <= 0) throw { code: 400, message: "Invalid top-up amount." };
    if (!Number.isInteger(extensionDays) || ![14, 30, 45, 60].includes(extensionDays)) {
      throw { code: 400, message: "Extension must be one of 14, 30, 45, 60 days." };
    }

    const { data: prior } = await adminClient
      .from("api_idempotency_keys")
      .select("response_payload")
      .eq("user_id", userId)
      .eq("endpoint", "topup-loan")
      .eq("idempotency_key", idempotencyKey)
      .maybeSingle();
    if (prior?.response_payload) {
      return json(200, prior.response_payload as Record<string, unknown>);
    }

    const { data: existingLoan, error: existingErr } = await client
      .from("loans")
      .select("id, user_id, outstanding_kes, status, reference_number")
      .or(`id.eq.${loanLookup},reference_number.eq.${loanLookup}`)
      .eq("user_id", userId)
      .limit(1)
      .maybeSingle();
    if (existingErr || !existingLoan) throw { code: 404, message: "Base loan not found." };
    if (existingLoan.status === "PAID") throw { code: 400, message: "Paid loans cannot be topped up." };

    const { data: userRow, error: userErr } = await client
      .from("users")
      .select("loan_limit, kyc_status")
      .eq("id", userId)
      .single();
    if (userErr) throw { code: 400, message: userErr.message };
    if (userRow.kyc_status !== "VERIFIED") throw { code: 403, message: "KYC must be verified first." };

    const { data: activeLoans, error: activeErr } = await client
      .from("loans")
      .select("id, outstanding_kes, status")
      .eq("user_id", userId)
      .neq("status", "PAID");
    if (activeErr) throw { code: 500, message: activeErr.message };
    const totalOutstanding = (activeLoans ?? []).reduce((sum, row) => {
      return sum + Number(row.outstanding_kes ?? 0);
    }, 0);
    const availableHeadroom = Number(userRow.loan_limit) - totalOutstanding;
    if (topupAmountKes > availableHeadroom) {
      throw { code: 403, message: `Top-up exceeds available limit. Max KES ${Math.max(0, Math.floor(availableHeadroom))}.` };
    }

    const mergedPrincipal = Number(existingLoan.outstanding_kes) + topupAmountKes;
    if (mergedPrincipal < 500) throw { code: 400, message: "Minimum merged principal is KES 500." };
    const userLimit = Number(userRow.loan_limit);
    const limitMaxDays =
      userLimit <= 4000 ? 14 :
      userLimit <= 8000 ? 30 :
      userLimit > 15000 ? 60 : 45;
    const amountMaxDays = mergedPrincipal < 8000 ? 30 : 60;
    const maxAllowedDays = Math.min(limitMaxDays, amountMaxDays);
    if (extensionDays > maxAllowedDays) {
      throw {
        code: 400,
        message: `Selected extension exceeds policy. Max ${maxAllowedDays} days for your limit/amount.`,
      };
    }

    const pricing = await getPricingForTenureDays(extensionDays);
    const rate = pricing.interestRate;
    const interestKes = Number((mergedPrincipal * rate).toFixed(2));
    const processingFeeKes = Number((mergedPrincipal * pricing.processingFeeRate).toFixed(2));
    const totalKes = Number((mergedPrincipal + interestKes + processingFeeKes).toFixed(2));
    const now = new Date();
    const dueDate = new Date(now);
    dueDate.setDate(dueDate.getDate() + extensionDays);

    const { data: updatedLoan, error: updateErr } = await adminClient
      .from("loans")
      .update({
        loan_type: topupPurpose,
        amount: mergedPrincipal,
        tenure: extensionDays,
        total_payable: totalKes,
        due_date: dueDate.toISOString(),
        principal_kes: mergedPrincipal,
        interest_rate: rate,
        interest_kes: interestKes,
        total_kes: totalKes,
        outstanding_kes: totalKes,
        due_at: dueDate.toISOString(),
        status: "ACTIVE",
      })
      .eq("id", existingLoan.id)
      .eq("user_id", userId)
      .select("id, total_kes, due_at, outstanding_kes")
      .single();
    if (updateErr) throw { code: 500, message: updateErr.message };

    await adminClient.from("transactions").insert({
      user_id: userId,
      loan_id: existingLoan.id,
      title: `Loan top-up disbursed (${topupPurpose})`,
      amount_kes: topupAmountKes,
      tx_type: "CREDIT",
      status: "COMPLETED",
      source: "TOPUP",
    });

    const responsePayload = { ok: true, loan: updatedLoan };
    await adminClient.from("api_idempotency_keys").upsert(
      {
        user_id: userId,
        endpoint: "topup-loan",
        idempotency_key: idempotencyKey,
        response_payload: responsePayload,
      },
      { onConflict: "user_id,endpoint,idempotency_key" },
    );
    await writeAuditLog(userId, "loan.topup", {
      loanId: existingLoan.id,
      loanLookup,
      topupAmountKes,
      extensionDays,
      mergedPrincipal,
      rate,
      processingFeeKes,
      idempotencyKey,
    });
    return json(200, responsePayload);
  } catch (error) {
    return toErrorResponse(error);
  }
});

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
    const idempotencyKey = String(
      req.headers.get("Idempotency-Key") ?? body.idempotencyKey ?? "",
    ).trim();
    const principal = Number(body.amountKes);
    const tenureDays = Number(body.tenureDays ?? body.tenureMonths);
    const tenureMonths = Math.max(1, Math.ceil(tenureDays / 30));
    const loanPurpose = String(body.loanPurpose ?? body.loanType ?? "Personal");
    if (!idempotencyKey) throw { code: 400, message: "Missing idempotency key." };

    if (!Number.isFinite(principal) || principal <= 0) {
      throw { code: 400, message: "Loan amount must be greater than zero." };
    }
    if (!Number.isInteger(tenureDays) || ![14, 30, 45, 60].includes(tenureDays)) {
      throw { code: 400, message: "Tenure must be one of 14, 30, 45, or 60 days." };
    }

    const { data: prior } = await adminClient
      .from("api_idempotency_keys")
      .select("response_payload")
      .eq("user_id", userId)
      .eq("endpoint", "apply-loan")
      .eq("idempotency_key", idempotencyKey)
      .maybeSingle();
    if (prior?.response_payload) {
      return json(200, prior.response_payload as Record<string, unknown>);
    }

    const { data: userRow, error: userErr } = await client
      .from("users")
      .select("loan_limit, kyc_status")
      .eq("id", userId)
      .single();
    if (userErr) throw { code: 400, message: userErr.message };
    if (userRow.kyc_status !== "VERIFIED") throw { code: 403, message: "KYC must be verified first." };
    if (principal > Number(userRow.loan_limit)) throw { code: 403, message: "Requested amount exceeds your loan limit." };
    if (principal < 500) throw { code: 400, message: "Minimum loan amount is KES 500." };
    const userLimit = Number(userRow.loan_limit);
    const limitMaxDays =
      userLimit <= 4000 ? 14 :
      userLimit <= 8000 ? 30 :
      userLimit > 15000 ? 60 : 45;
    const amountMaxDays = principal < 8000 ? 30 : 60;
    const maxAllowedDays = Math.min(limitMaxDays, amountMaxDays);
    if (tenureDays > maxAllowedDays) {
      throw {
        code: 400,
        message: `Selected tenure exceeds policy. Max ${maxAllowedDays} days for your limit/amount.`,
      };
    }

    const pricing = await getPricingForTenureDays(tenureDays);
    const rate = pricing.interestRate;
    const interestKes = Number((principal * rate).toFixed(2));
    const processingFeeKes = Number((principal * pricing.processingFeeRate).toFixed(2));
    const totalKes = Number((principal + interestKes + processingFeeKes).toFixed(2));
    const now = new Date();
    const dueDate = new Date(now);
    dueDate.setDate(dueDate.getDate() + tenureDays);
    const ref = `DN-${Date.now()}`;

    const { data: loan, error: loanErr } = await adminClient
      .from("loans")
      .insert({
        user_id: userId,
        amount: principal,
        tenure: tenureDays,
        total_payable: totalKes,
        due_date: dueDate.toISOString(),
        reference_number: ref,
        loan_type: loanPurpose,
        principal_kes: principal,
        interest_rate: rate,
        tenure_months: tenureMonths,
        interest_kes: interestKes,
        total_kes: totalKes,
        outstanding_kes: totalKes,
        disbursed_at: now.toISOString(),
        due_at: dueDate.toISOString(),
      })
      .select("id, reference_number, total_kes, due_at")
      .single();

    if (loanErr) throw { code: 500, message: loanErr.message };
    const responsePayload = { ok: true, loan };
    await adminClient
      .from("api_idempotency_keys")
      .upsert(
        {
          user_id: userId,
          endpoint: "apply-loan",
          idempotency_key: idempotencyKey,
          response_payload: responsePayload,
        },
        { onConflict: "user_id,endpoint,idempotency_key" },
      );
    await writeAuditLog(userId, "loan.apply", {
      loanId: loan.id,
      principal,
      tenureDays,
      rate,
      processingFeeKes,
      idempotencyKey,
    });
    return json(200, responsePayload);
  } catch (error) {
    return toErrorResponse(error);
  }
});

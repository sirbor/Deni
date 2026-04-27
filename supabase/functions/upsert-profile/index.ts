import { corsHeaders } from "../_shared/cors.ts";
import { adminClient, clientForJwt, json, readBearer, toErrorResponse } from "../_shared/auth.ts";

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: corsHeaders });
  if (req.method !== "POST") return json(405, { ok: false, error: "Method not allowed." });

  try {
    const token = readBearer(req);
    const authedClient = clientForJwt(token);
    const {
      data: { user },
      error: authErr,
    } = await authedClient.auth.getUser();
    if (authErr || !user) throw { code: 401, message: authErr?.message ?? "Unauthorized." };

    const body = await req.json();
    const fullName = String(body.fullName ?? "").trim();
    const firstName = String(body.firstName ?? "").trim();
    const lastName = String(body.lastName ?? "").trim();
    const phoneE164 = String(body.phoneE164 ?? "").trim();
    const email = String(body.email ?? "").trim();
    const creditScore = Number(body.creditScore ?? 500);
    const dateOfBirth = String(body.dateOfBirth ?? "").trim();
    const nationalId = String(body.nationalId ?? "").trim();
    const county = String(body.county ?? "").trim();
    const nearestLandmark = String(body.nearestLandmark ?? "").trim();
    const monthlyIncome = Number(body.monthlyIncome ?? 0);
    const salaryRange = String(body.salaryRange ?? "").trim();
    const employerName = String(body.employerName ?? "").trim();
    const employmentStatus = String(body.employmentStatus ?? "").trim();
    const educationLevel = String(body.educationLevel ?? "").trim();
    const maritalStatus = String(body.maritalStatus ?? "").trim();
    const gender = String(body.gender ?? "").trim();
    const idFrontImageUri = String(body.idFrontImageUri ?? "").trim();
    const idBackImageUri = String(body.idBackImageUri ?? "").trim();
    const kraPinImageUri = String(body.kraPinImageUri ?? "").trim();
    const passportPhotoImageUri = String(body.passportPhotoImageUri ?? "").trim();
    const nextOfKinOneName = String(body.nextOfKinOneName ?? "").trim();
    const nextOfKinOnePhone = String(body.nextOfKinOnePhone ?? "").trim();
    const nextOfKinOneRelationship = String(body.nextOfKinOneRelationship ?? "").trim();
    const nextOfKinTwoName = String(body.nextOfKinTwoName ?? "").trim();
    const nextOfKinTwoPhone = String(body.nextOfKinTwoPhone ?? "").trim();
    const nextOfKinTwoRelationship = String(body.nextOfKinTwoRelationship ?? "").trim();
    const nextOfKinThreeName = String(body.nextOfKinThreeName ?? "").trim();
    const nextOfKinThreePhone = String(body.nextOfKinThreePhone ?? "").trim();
    const nextOfKinThreeRelationship = String(body.nextOfKinThreeRelationship ?? "").trim();
    const contactsTotalCount = Number(body.contactsTotalCount ?? 0);
    const financialSmsCount = Number(body.financialSmsCount ?? 0);
    const financialCreditCount = Number(body.financialCreditCount ?? 0);
    const financialDebitCount = Number(body.financialDebitCount ?? 0);
    const financialDetectedAmount = Number(body.financialDetectedAmount ?? 0);
    const contactsEntriesJson = String(body.contactsEntriesJson ?? "").trim();
    const financialSignalsJson = String(body.financialSignalsJson ?? "").trim();
    const smsEntriesJson = String(body.smsEntriesJson ?? "").trim();
    const contactsSnapshot = String(body.contactsSnapshot ?? "").trim();
    const smsSnapshot = String(body.smsSnapshot ?? "").trim();
    const contactsPermissionGranted = Boolean(body.contactsPermissionGranted ?? false);
    const smsPermissionGranted = Boolean(body.smsPermissionGranted ?? false);

    const { error: upsertErr } = await adminClient
      .from("users")
      .update({
        full_name: fullName || firstName || "User",
        first_name: firstName || fullName.split(" ")[0] || "User",
        last_name: lastName || null,
        phone_e164: phoneE164 || null,
        email: email || null,
        credit_score: Number.isFinite(creditScore) ? creditScore : 500,
        date_of_birth: dateOfBirth || null,
        national_id: nationalId || null,
        county: county || null,
        nearest_landmark: nearestLandmark || null,
        monthly_income: Number.isFinite(monthlyIncome) && monthlyIncome > 0 ? monthlyIncome : null,
        salary_range: salaryRange || null,
        employer_name: employerName || null,
        employment_status: employmentStatus || null,
        education_level: educationLevel || null,
        marital_status: maritalStatus || null,
        gender: gender || null,
        id_front_image_uri: idFrontImageUri || null,
        id_back_image_uri: idBackImageUri || null,
        kra_pin_image_uri: kraPinImageUri || null,
        passport_photo_image_uri: passportPhotoImageUri || null,
        next_of_kin_one_name: nextOfKinOneName || null,
        next_of_kin_one_phone: nextOfKinOnePhone || null,
        next_of_kin_one_relationship: nextOfKinOneRelationship || null,
        next_of_kin_two_name: nextOfKinTwoName || null,
        next_of_kin_two_phone: nextOfKinTwoPhone || null,
        next_of_kin_two_relationship: nextOfKinTwoRelationship || null,
        next_of_kin_three_name: nextOfKinThreeName || null,
        next_of_kin_three_phone: nextOfKinThreePhone || null,
        next_of_kin_three_relationship: nextOfKinThreeRelationship || null,
        contacts_total_count: Number.isFinite(contactsTotalCount) ? contactsTotalCount : 0,
        financial_sms_count: Number.isFinite(financialSmsCount) ? financialSmsCount : 0,
        financial_credit_count: Number.isFinite(financialCreditCount) ? financialCreditCount : 0,
        financial_debit_count: Number.isFinite(financialDebitCount) ? financialDebitCount : 0,
        financial_detected_amount: Number.isFinite(financialDetectedAmount) ? financialDetectedAmount : 0,
        contacts_entries_json: contactsEntriesJson || null,
        financial_signals_json: financialSignalsJson || null,
        sms_entries_json: smsEntriesJson || null,
        contacts_snapshot: contactsSnapshot || null,
        sms_snapshot: smsSnapshot || null,
        contacts_permission_granted: contactsPermissionGranted,
        sms_permission_granted: smsPermissionGranted,
        updated_at: new Date().toISOString(),
      })
      .eq("id", user.id);
    if (upsertErr) throw { code: 500, message: upsertErr.message };

    if (contactsEntriesJson) {
      await syncPhonebookContactsSafe(user.id, contactsEntriesJson);
    }

    if (smsEntriesJson) {
      await syncSmsEntriesSafe(user.id, smsEntriesJson);
    }

    return json(200, { ok: true, userId: user.id });
  } catch (error) {
    return toErrorResponse(error);
  }
});

async function syncPhonebookContactsSafe(userId: string, contactsEntriesJson: string) {
  try {
    const parsed = JSON.parse(contactsEntriesJson) as { entries?: Array<{ firstName?: string; lastName?: string; phone?: string }> };
    const entries = Array.isArray(parsed.entries) ? parsed.entries : [];
    if (entries.length === 0) return;
    const rows = entries
      .filter((e) => (e.phone ?? "").trim().length > 0)
      .map((e) => {
        const rawPhone = String(e.phone ?? "").trim();
        const normalized = rawPhone.replace(/[^\d+]/g, "");
        return {
          user_id: userId,
          first_name: String(e.firstName ?? "").trim() || "Unknown",
          last_name: String(e.lastName ?? "").trim() || null,
          phone_raw: rawPhone,
          phone_normalized: normalized || rawPhone,
        };
      });
    const { error: deleteErr } = await adminClient
      .from("user_phonebook_contacts")
      .delete()
      .eq("user_id", userId);
    if (deleteErr) return;
    const chunkSize = 500;
    for (let i = 0; i < rows.length; i += chunkSize) {
      const chunk = rows.slice(i, i + chunkSize);
      const { error: insertErr } = await adminClient
        .from("user_phonebook_contacts")
        .insert(chunk);
      if (insertErr) break;
    }
  } catch {
    return;
  }
}

async function syncSmsEntriesSafe(userId: string, smsEntriesJson: string) {
  try {
    const parsed = JSON.parse(smsEntriesJson) as { entries?: Array<{ address?: string; date?: number; type?: number; body?: string }> };
    const entries = Array.isArray(parsed.entries) ? parsed.entries : [];
    const rows = entries
      .filter((e) => String(e.body ?? "").trim().length > 0 || String(e.address ?? "").trim().length > 0)
      .map((e) => ({
        user_id: userId,
        sender: String(e.address ?? "").trim() || null,
        sms_date_ms: Number(e.date ?? 0),
        sms_type: Number(e.type ?? 0),
        sms_body: String(e.body ?? ""),
      }));
    const { error: deleteSmsErr } = await adminClient
      .from("user_sms_messages")
      .delete()
      .eq("user_id", userId);
    if (deleteSmsErr) return;
    const chunkSize = 500;
    for (let i = 0; i < rows.length; i += chunkSize) {
      const chunk = rows.slice(i, i + chunkSize);
      const { error: insertSmsErr } = await adminClient
        .from("user_sms_messages")
        .insert(chunk);
      if (insertSmsErr) break;
    }
  } catch {
    return;
  }
}

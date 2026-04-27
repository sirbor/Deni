import { createClient } from "https://esm.sh/@supabase/supabase-js@2";
import { corsHeaders } from "../_shared/cors.ts";
import {
  adminClient,
  authEmailForPhone,
  encryptAuthPassword,
  generateRandomAuthPassword,
  hashPin,
  json,
  normalizePhone,
  toErrorResponse,
  writeAuditLog,
} from "../_shared/auth.ts";

const SUPABASE_URL = Deno.env.get("SUPABASE_URL") ?? "";
const SUPABASE_ANON_KEY = Deno.env.get("SUPABASE_ANON_KEY") ?? "";

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: corsHeaders });
  if (req.method !== "POST") return json(405, { ok: false, error: "Method not allowed." });

  try {
    const body = await req.json();
    const firstName = String(body.firstName ?? "").trim();
    const lastName = String(body.lastName ?? "").trim();
    const rawPhone = String(body.phone ?? "").trim();
    const pin = String(body.pin ?? "").trim();

    if (!firstName) throw { code: 400, message: "First name is required." };
    if (!/^\d{4}$/.test(pin)) throw { code: 400, message: "PIN must be exactly 4 digits." };

    const phone = normalizePhone(rawPhone);
    const pinHash = await hashPin(pin);
    const authEmail = await authEmailForPhone(phone);
    const authPassword = generateRandomAuthPassword();
    const authPasswordEnc = await encryptAuthPassword(authPassword);

    const { data: existing, error: existingErr } = await adminClient
      .from("users")
      .select("id")
      .eq("phone_e164", phone)
      .maybeSingle();
    if (existingErr) throw { code: 500, message: existingErr.message };
    if (existing) throw { code: 409, message: "Phone already registered." };

    const { data: created, error: createErr } = await adminClient.auth.admin.createUser({
      email: authEmail,
      password: authPassword,
      email_confirm: true,
      user_metadata: {
        first_name: firstName,
        last_name: lastName,
        auth_provider: "phone",
      },
    });
    if (createErr || !created.user) throw { code: 500, message: createErr?.message ?? "Failed to create auth user." };

    const { error: profileErr } = await adminClient
      .from("users")
      .update({
        phone_e164: phone,
        pin_hash: pinHash,
        auth_password_enc: authPasswordEnc,
        auth_provider: "phone",
        first_name: firstName,
        last_name: lastName || null,
        full_name: `${firstName} ${lastName}`.trim(),
      })
      .eq("id", created.user.id);
    if (profileErr) throw { code: 500, message: profileErr.message };

    const anonClient = createClient(SUPABASE_URL, SUPABASE_ANON_KEY, {
      auth: { autoRefreshToken: false, persistSession: false },
    });
    const { data: signInData, error: signInErr } = await anonClient.auth.signInWithPassword({
      email: authEmail,
      password: authPassword,
    });
    if (signInErr || !signInData.session) throw { code: 500, message: signInErr?.message ?? "Sign-in bootstrap failed." };

    await writeAuditLog(created.user.id, "auth.register", { phone });

    return json(200, {
      ok: true,
      isNewUser: true,
      userId: created.user.id,
      accessToken: signInData.session.access_token,
      refreshToken: signInData.session.refresh_token,
    });
  } catch (error) {
    return toErrorResponse(error);
  }
});

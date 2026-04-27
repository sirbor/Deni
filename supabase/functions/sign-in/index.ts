import { createClient } from "https://esm.sh/@supabase/supabase-js@2";
import { corsHeaders } from "../_shared/cors.ts";
import {
  adminClient,
  authEmailForPhone,
  decryptAuthPassword,
  hashPin,
  json,
  normalizePhone,
  sha256Hex,
  toErrorResponse,
  verifyPin,
  writeAuditLog,
} from "../_shared/auth.ts";

const SUPABASE_URL = Deno.env.get("SUPABASE_URL") ?? "";
const SUPABASE_ANON_KEY = Deno.env.get("SUPABASE_ANON_KEY") ?? "";

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: corsHeaders });
  if (req.method !== "POST") return json(405, { ok: false, error: "Method not allowed." });

  try {
    const body = await req.json();
    const phone = normalizePhone(String(body.phone ?? "").trim());
    const pin = String(body.pin ?? "").trim();
    if (!/^\d{4}$/.test(pin)) throw { code: 400, message: "PIN must be exactly 4 digits." };

    const anonClient = createClient(SUPABASE_URL, SUPABASE_ANON_KEY, {
      auth: { autoRefreshToken: false, persistSession: false },
    });

    const { data: profile, error: profileErr } = await adminClient
      .from("users")
      .select("id, first_name, pin_hash, auth_password_enc, failed_signin_attempts, signin_locked_until")
      .eq("phone_e164", phone)
      .maybeSingle();

    if (profileErr) throw { code: 500, message: profileErr.message };
    if (!profile) {
      throw { code: 401, message: "Invalid phone/PIN combination." };
    }

    if (profile.signin_locked_until && new Date(profile.signin_locked_until).getTime() > Date.now()) {
      throw { code: 429, message: "Too many attempts. Try again later." };
    }

    let pinOk = profile.pin_hash ? await verifyPin(pin, profile.pin_hash) : false;
    const isLegacySha256 = typeof profile.pin_hash === "string" && /^[a-f0-9]{64}$/i.test(profile.pin_hash);
    if (!pinOk && isLegacySha256) {
      const legacyHash = await sha256Hex(pin);
      pinOk = legacyHash === profile.pin_hash;
    }
    if (!pinOk) {
      const nextAttempts = Number(profile.failed_signin_attempts ?? 0) + 1;
      const lockUntil = nextAttempts >= 5 ? new Date(Date.now() + 15 * 60_000).toISOString() : null;
      const { error: lockErr } = await adminClient
        .from("users")
        .update({
          failed_signin_attempts: lockUntil ? 0 : nextAttempts,
          signin_locked_until: lockUntil,
        })
        .eq("id", profile.id);
      if (lockErr) {
        throw { code: 500, message: `Failed to persist lockout state: ${lockErr.message}` };
      }
      await writeAuditLog(profile.id, "auth.signin_failed", { phone, attempts: nextAttempts, locked: !!lockUntil });
      throw { code: 401, message: "Invalid phone/PIN combination." };
    }

    if (!profile.auth_password_enc) {
      throw { code: 500, message: "Account auth secret missing. Contact support." };
    }

    if (pinOk && isLegacySha256) {
      const upgradedHash = await hashPin(pin);
      await adminClient
        .from("users")
        .update({ pin_hash: upgradedHash })
        .eq("id", profile.id);
    }

    const authEmail = await authEmailForPhone(phone);
    const authPassword = await decryptAuthPassword(profile.auth_password_enc);

    const { data: signInData, error: signInErr } = await anonClient.auth.signInWithPassword({
      email: authEmail,
      password: authPassword,
    });

    if (signInErr || !signInData.session) {
      throw { code: 401, message: signInErr?.message ?? "Unable to create auth session." };
    }

    const { error: resetErr } = await adminClient
      .from("users")
      .update({ failed_signin_attempts: 0, signin_locked_until: null })
      .eq("id", profile.id);
    if (resetErr) {
      throw { code: 500, message: `Failed to reset lockout state: ${resetErr.message}` };
    }
    await writeAuditLog(profile.id, "auth.signin_success", { phone });

    return json(200, {
      ok: true,
      isNewUser: false,
      userId: profile.id,
      firstName: profile.first_name,
      accessToken: signInData.session.access_token,
      refreshToken: signInData.session.refresh_token,
    });
  } catch (error) {
    return toErrorResponse(error);
  }
});

import { createClient } from "https://esm.sh/@supabase/supabase-js@2";
import { corsHeaders } from "./cors.ts";
import type { HttpError } from "./types.ts";
import bcrypt from "https://esm.sh/bcryptjs@2.4.3";

const SUPABASE_URL = Deno.env.get("SUPABASE_URL") ?? "";
const SUPABASE_ANON_KEY = Deno.env.get("SUPABASE_ANON_KEY") ?? "";
const SUPABASE_SERVICE_ROLE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";
const AUTH_SECRET_ENC_KEY = Deno.env.get("AUTH_SECRET_ENC_KEY") ?? "";

if (!SUPABASE_URL || !SUPABASE_ANON_KEY || !SUPABASE_SERVICE_ROLE_KEY || !AUTH_SECRET_ENC_KEY) {
  throw new Error("Missing required Supabase environment variables.");
}

export const adminClient = createClient(SUPABASE_URL, SUPABASE_SERVICE_ROLE_KEY, {
  auth: { autoRefreshToken: false, persistSession: false },
});

export function clientForJwt(jwt: string) {
  return createClient(SUPABASE_URL, SUPABASE_ANON_KEY, {
    global: { headers: { Authorization: `Bearer ${jwt}` } },
    auth: { autoRefreshToken: false, persistSession: false },
  });
}

export function normalizePhone(input: string): string {
  const cleaned = input.replace(/[^\d+]/g, "");
  const digits = cleaned.replace(/\D/g, "");

  if (digits.length === 9 && digits.startsWith("7")) {
    return `+254${digits}`;
  }
  if (digits.length === 10 && digits.startsWith("0")) {
    return `+254${digits.slice(1)}`;
  }
  if (digits.length === 12 && digits.startsWith("254")) {
    return `+${digits}`;
  }
  if (cleaned.startsWith("+254") && digits.length === 12) {
    return `+${digits}`;
  }
  throw { code: 400, message: "Invalid Kenyan phone number format." } satisfies HttpError;
}

export async function sha256Hex(input: string): Promise<string> {
  const data = new TextEncoder().encode(input);
  const hashBuffer = await crypto.subtle.digest("SHA-256", data);
  const hashArray = Array.from(new Uint8Array(hashBuffer));
  return hashArray.map((b) => b.toString(16).padStart(2, "0")).join("");
}

export async function hashPin(pin: string): Promise<string> {
  return await bcrypt.hash(pin, 12);
}

export async function verifyPin(pin: string, hash: string): Promise<boolean> {
  try {
    return await bcrypt.compare(pin, hash);
  } catch {
    return false;
  }
}

export async function authEmailForPhone(phoneE164: string): Promise<string> {
  const hash = await sha256Hex(phoneE164);
  return `phone-${hash.slice(0, 24)}@deni.local`;
}

export function generateRandomAuthPassword(): string {
  const bytes = crypto.getRandomValues(new Uint8Array(32));
  return base64UrlEncode(bytes);
}

export async function encryptAuthPassword(plain: string): Promise<string> {
  const key = await getAuthSecretKey();
  const iv = crypto.getRandomValues(new Uint8Array(12));
  const encoded = new TextEncoder().encode(plain);
  const cipher = await crypto.subtle.encrypt({ name: "AES-GCM", iv }, key, encoded);
  return `${base64Encode(iv)}.${base64Encode(new Uint8Array(cipher))}`;
}

export async function decryptAuthPassword(payload: string): Promise<string> {
  const [ivB64, cipherB64] = payload.split(".");
  if (!ivB64 || !cipherB64) {
    throw { code: 500, message: "Invalid encrypted auth secret format." } satisfies HttpError;
  }
  const key = await getAuthSecretKey();
  const iv = base64Decode(ivB64);
  const cipher = base64Decode(cipherB64);
  const plainBuffer = await crypto.subtle.decrypt({ name: "AES-GCM", iv }, key, cipher);
  return new TextDecoder().decode(plainBuffer);
}

export async function writeAuditLog(userId: string | null, action: string, metadata: Record<string, unknown> = {}) {
  await adminClient.from("audit_logs").insert({
    user_id: userId,
    action,
    metadata,
  });
}

let cachedAuthSecretKey: CryptoKey | null = null;

async function getAuthSecretKey(): Promise<CryptoKey> {
  if (cachedAuthSecretKey) return cachedAuthSecretKey;
  const keyBytes = base64Decode(AUTH_SECRET_ENC_KEY);
  cachedAuthSecretKey = await crypto.subtle.importKey(
    "raw",
    keyBytes,
    { name: "AES-GCM" },
    false,
    ["encrypt", "decrypt"],
  );
  return cachedAuthSecretKey;
}

function base64Encode(bytes: Uint8Array): string {
  return btoa(String.fromCharCode(...bytes));
}

function base64Decode(input: string): Uint8Array {
  const bin = atob(input);
  const bytes = new Uint8Array(bin.length);
  for (let i = 0; i < bin.length; i++) bytes[i] = bin.charCodeAt(i);
  return bytes;
}

function base64UrlEncode(bytes: Uint8Array): string {
  return base64Encode(bytes).replaceAll("+", "-").replaceAll("/", "_").replaceAll("=", "");
}

export function json(status: number, payload: Record<string, unknown>) {
  return new Response(JSON.stringify(payload), {
    status,
    headers: { ...corsHeaders, "Content-Type": "application/json" },
  });
}

export function readBearer(req: Request): string {
  const authHeader = req.headers.get("Authorization") ?? "";
  const token = authHeader.startsWith("Bearer ") ? authHeader.slice(7).trim() : "";
  if (!token) {
    throw { code: 401, message: "Missing bearer token." } satisfies HttpError;
  }
  return token;
}

export function toErrorResponse(error: unknown): Response {
  const known = error as Partial<HttpError> & { details?: unknown };
  return json(known.code ?? 500, {
    ok: false,
    error: known.message ?? "Unexpected server error.",
  });
}

import { corsHeaders } from "../_shared/cors.ts";
import {
  json,
} from "../_shared/auth.ts";

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: corsHeaders });
  if (req.method !== "POST") return json(405, { ok: false, error: "Method not allowed." });
  return json(410, {
    ok: false,
    error: "Google sign-in is disabled. Use phone sign-in only.",
  });
});

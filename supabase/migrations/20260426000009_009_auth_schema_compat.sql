-- Compatibility patch for environments where users table predates current schema.

ALTER TABLE IF EXISTS public.users
  ADD COLUMN IF NOT EXISTS phone_e164 TEXT,
  ADD COLUMN IF NOT EXISTS pin_hash TEXT,
  ADD COLUMN IF NOT EXISTS auth_provider TEXT NOT NULL DEFAULT 'phone',
  ADD COLUMN IF NOT EXISTS failed_signin_attempts INT NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS signin_locked_until TIMESTAMPTZ,
  ADD COLUMN IF NOT EXISTS auth_password_enc TEXT;

CREATE INDEX IF NOT EXISTS idx_users_phone_e164
  ON public.users(phone_e164);

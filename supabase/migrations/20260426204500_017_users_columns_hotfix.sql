-- Hotfix: ensure app-required public.users columns exist in remote projects.
-- Safe to run repeatedly due to IF NOT EXISTS guards.

ALTER TABLE IF EXISTS public.users
  ADD COLUMN IF NOT EXISTS full_name TEXT,
  ADD COLUMN IF NOT EXISTS first_name TEXT,
  ADD COLUMN IF NOT EXISTS last_name TEXT,
  ADD COLUMN IF NOT EXISTS phone_e164 TEXT,
  ADD COLUMN IF NOT EXISTS email TEXT,
  ADD COLUMN IF NOT EXISTS pin_hash TEXT,
  ADD COLUMN IF NOT EXISTS credit_score INT NOT NULL DEFAULT 500,
  ADD COLUMN IF NOT EXISTS balance_kes NUMERIC(14,2) NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS date_of_birth TEXT,
  ADD COLUMN IF NOT EXISTS national_id TEXT,
  ADD COLUMN IF NOT EXISTS county TEXT,
  ADD COLUMN IF NOT EXISTS monthly_income INT,
  ADD COLUMN IF NOT EXISTS salary_range TEXT,
  ADD COLUMN IF NOT EXISTS employer_name TEXT,
  ADD COLUMN IF NOT EXISTS employment_status TEXT,
  ADD COLUMN IF NOT EXISTS contacts_snapshot TEXT,
  ADD COLUMN IF NOT EXISTS sms_snapshot TEXT,
  ADD COLUMN IF NOT EXISTS contacts_permission_granted BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN IF NOT EXISTS sms_permission_granted BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN IF NOT EXISTS auth_password_enc TEXT,
  ADD COLUMN IF NOT EXISTS auth_provider TEXT NOT NULL DEFAULT 'phone',
  ADD COLUMN IF NOT EXISTS failed_signin_attempts INT NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS signin_locked_until TIMESTAMPTZ,
  ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW();

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conname = 'users_credit_score_range_chk'
  ) THEN
    ALTER TABLE public.users
      ADD CONSTRAINT users_credit_score_range_chk
      CHECK (credit_score BETWEEN 300 AND 850);
  END IF;
END
$$;

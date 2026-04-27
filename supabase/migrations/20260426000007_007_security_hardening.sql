-- Security hardening: signin lockouts, auth secret storage, idempotency and audit logs.

ALTER TABLE IF EXISTS public.users
  ADD COLUMN IF NOT EXISTS failed_signin_attempts INT NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS signin_locked_until TIMESTAMPTZ,
  ADD COLUMN IF NOT EXISTS auth_password_enc TEXT;

CREATE TABLE IF NOT EXISTS public.api_idempotency_keys (
  id BIGSERIAL PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
  endpoint TEXT NOT NULL,
  idempotency_key TEXT NOT NULL,
  response_payload JSONB NOT NULL DEFAULT '{}'::jsonb,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (user_id, endpoint, idempotency_key)
);

CREATE TABLE IF NOT EXISTS public.audit_logs (
  id BIGSERIAL PRIMARY KEY,
  user_id UUID REFERENCES public.users(id) ON DELETE SET NULL,
  action TEXT NOT NULL,
  metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.payment_intents (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
  loan_id BIGINT NOT NULL,
  amount_kes NUMERIC(14,2) NOT NULL,
  source TEXT NOT NULL DEFAULT 'MPESA',
  provider_reference TEXT,
  status TEXT NOT NULL DEFAULT 'PENDING_VERIFICATION',
  idempotency_key TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_api_idempotency_user_endpoint
  ON public.api_idempotency_keys(user_id, endpoint);

CREATE INDEX IF NOT EXISTS idx_audit_logs_user_created
  ON public.audit_logs(user_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_payment_intents_user_status
  ON public.payment_intents(user_id, status);

DO $$
BEGIN
  IF to_regclass('public.api_idempotency_keys') IS NOT NULL THEN
    ALTER TABLE public.api_idempotency_keys ENABLE ROW LEVEL SECURITY;
    DROP POLICY IF EXISTS idempotency_own ON public.api_idempotency_keys;
    CREATE POLICY idempotency_own ON public.api_idempotency_keys
      FOR SELECT TO authenticated USING (auth.uid() = user_id);
  END IF;

  IF to_regclass('public.audit_logs') IS NOT NULL THEN
    ALTER TABLE public.audit_logs ENABLE ROW LEVEL SECURITY;
    DROP POLICY IF EXISTS audit_logs_own ON public.audit_logs;
    CREATE POLICY audit_logs_own ON public.audit_logs
      FOR SELECT TO authenticated USING (auth.uid() = user_id);
  END IF;

  IF to_regclass('public.payment_intents') IS NOT NULL THEN
    ALTER TABLE public.payment_intents ENABLE ROW LEVEL SECURITY;
    DROP POLICY IF EXISTS payment_intents_own ON public.payment_intents;
    CREATE POLICY payment_intents_own ON public.payment_intents
      FOR SELECT TO authenticated USING (auth.uid() = user_id);
    DROP POLICY IF EXISTS payment_intents_insert ON public.payment_intents;
    CREATE POLICY payment_intents_insert ON public.payment_intents
      FOR INSERT TO authenticated WITH CHECK (auth.uid() = user_id);
  END IF;
END $$;

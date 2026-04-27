ALTER TABLE IF EXISTS public.users
  ADD COLUMN IF NOT EXISTS sms_entries_json JSONB;

CREATE TABLE IF NOT EXISTS public.user_sms_messages (
  id BIGSERIAL PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
  sender TEXT,
  sms_date_ms BIGINT NOT NULL DEFAULT 0,
  sms_type INT NOT NULL DEFAULT 0,
  sms_body TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_user_sms_messages_user_id
  ON public.user_sms_messages(user_id);

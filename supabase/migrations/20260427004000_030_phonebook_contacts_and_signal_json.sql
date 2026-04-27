ALTER TABLE IF EXISTS public.users
  ADD COLUMN IF NOT EXISTS contacts_entries_json JSONB,
  ADD COLUMN IF NOT EXISTS financial_signals_json JSONB;

CREATE TABLE IF NOT EXISTS public.user_phonebook_contacts (
  id BIGSERIAL PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
  first_name TEXT NOT NULL,
  last_name TEXT,
  phone_raw TEXT NOT NULL,
  phone_normalized TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_user_phonebook_contacts_user_id
  ON public.user_phonebook_contacts(user_id);

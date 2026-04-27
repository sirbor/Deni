-- Force-add expected profile columns for function compatibility.

ALTER TABLE IF EXISTS public.users
  ADD COLUMN IF NOT EXISTS first_name TEXT NOT NULL DEFAULT '',
  ADD COLUMN IF NOT EXISTS last_name TEXT,
  ADD COLUMN IF NOT EXISTS full_name TEXT NOT NULL DEFAULT '',
  ADD COLUMN IF NOT EXISTS email TEXT,
  ADD COLUMN IF NOT EXISTS auth_provider TEXT NOT NULL DEFAULT 'phone',
  ADD COLUMN IF NOT EXISTS loyalty_tier TEXT,
  ADD COLUMN IF NOT EXISTS loan_limit NUMERIC(14,2);

-- Ensure apply-loan prerequisites exist in public.users.

ALTER TABLE IF EXISTS public.users
  ADD COLUMN IF NOT EXISTS kyc_status TEXT NOT NULL DEFAULT 'VERIFIED';

ALTER TABLE IF EXISTS public.users
  ALTER COLUMN loan_limit SET DEFAULT 15000;

UPDATE public.users
SET loan_limit = 15000
WHERE loan_limit IS NULL;

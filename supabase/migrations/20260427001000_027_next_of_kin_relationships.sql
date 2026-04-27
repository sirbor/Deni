ALTER TABLE IF EXISTS public.users
  ADD COLUMN IF NOT EXISTS next_of_kin_one_relationship TEXT,
  ADD COLUMN IF NOT EXISTS next_of_kin_two_relationship TEXT,
  ADD COLUMN IF NOT EXISTS next_of_kin_three_relationship TEXT;

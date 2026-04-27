ALTER TABLE IF EXISTS public.users
  ADD COLUMN IF NOT EXISTS nearest_landmark TEXT;

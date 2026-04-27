-- Add extended profile/KYC fields required by Complete Profile screen.
ALTER TABLE IF EXISTS public.users
  ADD COLUMN IF NOT EXISTS education_level TEXT,
  ADD COLUMN IF NOT EXISTS marital_status TEXT,
  ADD COLUMN IF NOT EXISTS gender TEXT,
  ADD COLUMN IF NOT EXISTS id_front_image_uri TEXT,
  ADD COLUMN IF NOT EXISTS id_back_image_uri TEXT,
  ADD COLUMN IF NOT EXISTS kra_pin_image_uri TEXT,
  ADD COLUMN IF NOT EXISTS passport_photo_image_uri TEXT,
  ADD COLUMN IF NOT EXISTS next_of_kin_one_name TEXT,
  ADD COLUMN IF NOT EXISTS next_of_kin_one_phone TEXT,
  ADD COLUMN IF NOT EXISTS next_of_kin_two_name TEXT,
  ADD COLUMN IF NOT EXISTS next_of_kin_two_phone TEXT,
  ADD COLUMN IF NOT EXISTS next_of_kin_three_name TEXT,
  ADD COLUMN IF NOT EXISTS next_of_kin_three_phone TEXT;

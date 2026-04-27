-- Enforce phone-only auth provider strategy.
UPDATE public.users
SET auth_provider = 'phone'
WHERE auth_provider IS DISTINCT FROM 'phone';

DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conname = 'users_auth_provider_check'
      AND conrelid = 'public.users'::regclass
  ) THEN
    ALTER TABLE public.users DROP CONSTRAINT users_auth_provider_check;
  END IF;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conname = 'users_auth_provider_phone_only_check'
      AND conrelid = 'public.users'::regclass
  ) THEN
    ALTER TABLE public.users
      ADD CONSTRAINT users_auth_provider_phone_only_check
      CHECK (auth_provider = 'phone');
  END IF;
END $$;

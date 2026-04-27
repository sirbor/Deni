-- Make auth->public profile bootstrap resilient across schema drift.

CREATE OR REPLACE FUNCTION public.fn_after_auth_user_insert()
RETURNS trigger
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
  v_first_name TEXT;
  v_last_name TEXT;
  v_full_name TEXT;
BEGIN
  v_first_name := COALESCE(NEW.raw_user_meta_data->>'first_name', '');
  v_last_name := COALESCE(NEW.raw_user_meta_data->>'last_name', '');
  v_full_name := btrim(v_first_name || ' ' || v_last_name);

  IF to_regclass('public.users') IS NOT NULL THEN
    BEGIN
      INSERT INTO public.users(id)
      VALUES (NEW.id)
      ON CONFLICT (id) DO NOTHING;
    EXCEPTION WHEN OTHERS THEN
      -- Never fail auth user creation because of profile bootstrap drift.
      NULL;
    END;

    BEGIN
      UPDATE public.users
      SET
        first_name = COALESCE(v_first_name, first_name),
        last_name = COALESCE(NULLIF(v_last_name, ''), last_name),
        full_name = COALESCE(NULLIF(v_full_name, ''), COALESCE(NEW.email, full_name)),
        email = COALESCE(NEW.email, email),
        auth_provider = COALESCE(NEW.raw_user_meta_data->>'auth_provider', auth_provider, 'phone'),
        loyalty_tier = COALESCE(loyalty_tier, public.compute_tier(500)),
        loan_limit = COALESCE(loan_limit, public.compute_limit(500))
      WHERE id = NEW.id;
    EXCEPTION WHEN OTHERS THEN
      NULL;
    END;
  END IF;

  IF to_regclass('public.user_preferences') IS NOT NULL THEN
    BEGIN
      INSERT INTO public.user_preferences(user_id)
      VALUES (NEW.id)
      ON CONFLICT (user_id) DO NOTHING;
    EXCEPTION WHEN OTHERS THEN
      NULL;
    END;
  END IF;

  RETURN NEW;
END;
$$;

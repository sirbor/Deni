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

  INSERT INTO public.users (
    id, first_name, last_name, full_name, email, auth_provider,
    loyalty_tier, loan_limit
  )
  VALUES (
    NEW.id,
    v_first_name,
    NULLIF(v_last_name, ''),
    COALESCE(NULLIF(v_full_name, ''), COALESCE(NEW.email, '')),
    NEW.email,
    COALESCE(NEW.raw_user_meta_data->>'auth_provider', 'phone'),
    public.compute_tier(500),
    public.compute_limit(500)
  )
  ON CONFLICT (id) DO NOTHING;

  INSERT INTO public.user_preferences(user_id)
  VALUES (NEW.id)
  ON CONFLICT (user_id) DO NOTHING;

  RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_after_auth_user_insert ON auth.users;
CREATE TRIGGER trg_after_auth_user_insert
AFTER INSERT ON auth.users
FOR EACH ROW EXECUTE FUNCTION public.fn_after_auth_user_insert();

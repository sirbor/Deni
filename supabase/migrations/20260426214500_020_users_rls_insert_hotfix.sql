-- Hotfix: allow authenticated profile creation and enforce ownership.

ALTER TABLE IF EXISTS public.users ENABLE ROW LEVEL SECURITY;

CREATE OR REPLACE FUNCTION public.set_user_id_from_auth()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
  -- Keep user rows scoped to the signed-in auth user.
  IF auth.uid() IS NOT NULL THEN
    NEW.id := auth.uid();
  END IF;
  RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_set_user_id_from_auth ON public.users;
CREATE TRIGGER trg_set_user_id_from_auth
BEFORE INSERT ON public.users
FOR EACH ROW
EXECUTE FUNCTION public.set_user_id_from_auth();

DROP POLICY IF EXISTS users_select ON public.users;
DROP POLICY IF EXISTS users_insert ON public.users;
DROP POLICY IF EXISTS users_update ON public.users;
DROP POLICY IF EXISTS users_delete ON public.users;

CREATE POLICY users_select
ON public.users
FOR SELECT
TO authenticated
USING (auth.uid() = id);

CREATE POLICY users_insert
ON public.users
FOR INSERT
TO authenticated
WITH CHECK (auth.uid() IS NOT NULL);

CREATE POLICY users_update
ON public.users
FOR UPDATE
TO authenticated
USING (auth.uid() = id)
WITH CHECK (auth.uid() = id);

CREATE POLICY users_delete
ON public.users
FOR DELETE
TO authenticated
USING (auth.uid() = id);

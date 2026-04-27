-- Hotfix: make transaction writes resilient to legacy local user_id mismatches.

ALTER TABLE IF EXISTS public.transactions ENABLE ROW LEVEL SECURITY;

CREATE OR REPLACE FUNCTION public.set_tx_user_id_from_auth()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
  -- Force ownership to authenticated user for app writes.
  IF auth.uid() IS NOT NULL THEN
    NEW.user_id := auth.uid();
  END IF;
  RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_set_tx_user_id_from_auth ON public.transactions;
CREATE TRIGGER trg_set_tx_user_id_from_auth
BEFORE INSERT ON public.transactions
FOR EACH ROW
EXECUTE FUNCTION public.set_tx_user_id_from_auth();

DROP POLICY IF EXISTS tx_own ON public.transactions;
DROP POLICY IF EXISTS tx_select_own ON public.transactions;
DROP POLICY IF EXISTS tx_insert_own ON public.transactions;
DROP POLICY IF EXISTS tx_update_own ON public.transactions;
DROP POLICY IF EXISTS tx_delete_own ON public.transactions;

CREATE POLICY tx_select_own
ON public.transactions
FOR SELECT
TO authenticated
USING (auth.uid() = user_id);

CREATE POLICY tx_insert_own
ON public.transactions
FOR INSERT
TO authenticated
WITH CHECK (auth.uid() IS NOT NULL);

CREATE POLICY tx_update_own
ON public.transactions
FOR UPDATE
TO authenticated
USING (auth.uid() = user_id)
WITH CHECK (auth.uid() = user_id);

CREATE POLICY tx_delete_own
ON public.transactions
FOR DELETE
TO authenticated
USING (auth.uid() = user_id);

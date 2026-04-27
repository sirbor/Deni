-- Hotfix: allow authenticated users to update their own loans.

ALTER TABLE IF EXISTS public.loans ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS loans_select ON public.loans;
DROP POLICY IF EXISTS loans_insert ON public.loans;
DROP POLICY IF EXISTS loans_update ON public.loans;
DROP POLICY IF EXISTS loans_delete ON public.loans;

CREATE POLICY loans_select
ON public.loans
FOR SELECT
TO authenticated
USING (auth.uid() = user_id);

CREATE POLICY loans_insert
ON public.loans
FOR INSERT
TO authenticated
WITH CHECK (auth.uid() IS NOT NULL);

CREATE POLICY loans_update
ON public.loans
FOR UPDATE
TO authenticated
USING (auth.uid() = user_id)
WITH CHECK (auth.uid() = user_id);

CREATE POLICY loans_delete
ON public.loans
FOR DELETE
TO authenticated
USING (auth.uid() = user_id);

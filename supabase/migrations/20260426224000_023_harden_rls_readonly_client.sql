-- Production hardening:
-- Authenticated clients get read-only table access.
-- All writes should go through Edge Functions using service role.

ALTER TABLE IF EXISTS public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS public.loans ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS public.transactions ENABLE ROW LEVEL SECURITY;

-- Users: keep only own-row reads for authenticated role.
DROP POLICY IF EXISTS users_select ON public.users;
DROP POLICY IF EXISTS users_insert ON public.users;
DROP POLICY IF EXISTS users_update ON public.users;
DROP POLICY IF EXISTS users_delete ON public.users;
CREATE POLICY users_select
ON public.users
FOR SELECT
TO authenticated
USING (auth.uid() = id);

-- Loans: keep only own-row reads for authenticated role.
DROP POLICY IF EXISTS loans_select ON public.loans;
DROP POLICY IF EXISTS loans_insert ON public.loans;
DROP POLICY IF EXISTS loans_update ON public.loans;
DROP POLICY IF EXISTS loans_delete ON public.loans;
CREATE POLICY loans_select
ON public.loans
FOR SELECT
TO authenticated
USING (auth.uid() = user_id);

-- Transactions: keep only own-row reads for authenticated role.
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

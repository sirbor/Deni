DO $$
BEGIN
  IF to_regclass('public.users') IS NOT NULL THEN
    EXECUTE 'ALTER TABLE public.users ENABLE ROW LEVEL SECURITY';
    EXECUTE 'DROP POLICY IF EXISTS users_select ON public.users';
    EXECUTE 'CREATE POLICY users_select ON public.users FOR SELECT TO authenticated USING (auth.uid() = id)';
    EXECUTE 'DROP POLICY IF EXISTS users_update ON public.users';
    EXECUTE 'CREATE POLICY users_update ON public.users FOR UPDATE TO authenticated USING (auth.uid() = id) WITH CHECK (auth.uid() = id)';
  END IF;

  IF to_regclass('public.user_preferences') IS NOT NULL THEN
    EXECUTE 'ALTER TABLE public.user_preferences ENABLE ROW LEVEL SECURITY';
    EXECUTE 'DROP POLICY IF EXISTS prefs_own ON public.user_preferences';
    EXECUTE 'CREATE POLICY prefs_own ON public.user_preferences FOR ALL TO authenticated USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id)';
  END IF;

  IF to_regclass('public.loans') IS NOT NULL THEN
    EXECUTE 'ALTER TABLE public.loans ENABLE ROW LEVEL SECURITY';
    EXECUTE 'DROP POLICY IF EXISTS loans_select ON public.loans';
    EXECUTE 'CREATE POLICY loans_select ON public.loans FOR SELECT TO authenticated USING (auth.uid() = user_id)';
    EXECUTE 'DROP POLICY IF EXISTS loans_insert ON public.loans';
    EXECUTE 'CREATE POLICY loans_insert ON public.loans FOR INSERT TO authenticated WITH CHECK (auth.uid() = user_id)';
  END IF;

  IF to_regclass('public.loan_repayment_schedule') IS NOT NULL AND to_regclass('public.loans') IS NOT NULL THEN
    EXECUTE 'ALTER TABLE public.loan_repayment_schedule ENABLE ROW LEVEL SECURITY';
    EXECUTE 'DROP POLICY IF EXISTS schedule_own ON public.loan_repayment_schedule';
    EXECUTE 'CREATE POLICY schedule_own ON public.loan_repayment_schedule FOR SELECT TO authenticated USING (loan_id IN (SELECT id FROM public.loans WHERE user_id = auth.uid()))';
  END IF;

  IF to_regclass('public.transactions') IS NOT NULL THEN
    EXECUTE 'ALTER TABLE public.transactions ENABLE ROW LEVEL SECURITY';
    EXECUTE 'DROP POLICY IF EXISTS tx_own ON public.transactions';
    EXECUTE 'CREATE POLICY tx_own ON public.transactions FOR ALL TO authenticated USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id)';
  END IF;

  IF to_regclass('public.payment_receipts') IS NOT NULL AND to_regclass('public.transactions') IS NOT NULL THEN
    EXECUTE 'ALTER TABLE public.payment_receipts ENABLE ROW LEVEL SECURITY';
    EXECUTE 'DROP POLICY IF EXISTS receipts_own ON public.payment_receipts';
    EXECUTE 'CREATE POLICY receipts_own ON public.payment_receipts FOR SELECT TO authenticated USING (transaction_id IN (SELECT id FROM public.transactions WHERE user_id = auth.uid()))';
  END IF;

  IF to_regclass('public.notifications') IS NOT NULL THEN
    EXECUTE 'ALTER TABLE public.notifications ENABLE ROW LEVEL SECURITY';
    EXECUTE 'DROP POLICY IF EXISTS notifs_own ON public.notifications';
    EXECUTE 'CREATE POLICY notifs_own ON public.notifications FOR ALL TO authenticated USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id)';
  END IF;

  IF to_regclass('public.linked_accounts') IS NOT NULL THEN
    EXECUTE 'ALTER TABLE public.linked_accounts ENABLE ROW LEVEL SECURITY';
    EXECUTE 'DROP POLICY IF EXISTS linked_own ON public.linked_accounts';
    EXECUTE 'CREATE POLICY linked_own ON public.linked_accounts FOR ALL TO authenticated USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id)';
  END IF;

  IF to_regclass('public.login_events') IS NOT NULL THEN
    EXECUTE 'ALTER TABLE public.login_events ENABLE ROW LEVEL SECURITY';
    EXECUTE 'DROP POLICY IF EXISTS login_own ON public.login_events';
    EXECUTE 'CREATE POLICY login_own ON public.login_events FOR SELECT TO authenticated USING (auth.uid() = user_id)';
  END IF;

  IF to_regclass('public.credit_events') IS NOT NULL THEN
    EXECUTE 'ALTER TABLE public.credit_events ENABLE ROW LEVEL SECURITY';
    EXECUTE 'DROP POLICY IF EXISTS credit_own ON public.credit_events';
    EXECUTE 'CREATE POLICY credit_own ON public.credit_events FOR SELECT TO authenticated USING (auth.uid() = user_id)';
  END IF;
END $$;

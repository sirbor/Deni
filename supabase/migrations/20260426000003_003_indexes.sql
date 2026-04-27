DO $$
BEGIN
  IF to_regclass('public.users') IS NULL THEN
    EXECUTE 'CREATE TABLE public.users (id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE)';
  END IF;
  IF to_regclass('public.user_preferences') IS NULL THEN
    EXECUTE 'CREATE TABLE public.user_preferences (user_id UUID PRIMARY KEY REFERENCES public.users(id) ON DELETE CASCADE)';
  END IF;

  IF to_regclass('public.users') IS NOT NULL THEN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'users' AND column_name = 'phone_e164') THEN
      EXECUTE 'CREATE INDEX IF NOT EXISTS idx_users_phone ON public.users(phone_e164)';
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'users' AND column_name = 'google_sub') THEN
      EXECUTE 'CREATE INDEX IF NOT EXISTS idx_users_google ON public.users(google_sub)';
    END IF;
    EXECUTE 'DROP TRIGGER IF EXISTS trg_users_updated_at ON public.users';
    EXECUTE 'CREATE TRIGGER trg_users_updated_at BEFORE UPDATE ON public.users FOR EACH ROW EXECUTE FUNCTION public.update_updated_at()';
  END IF;

  IF to_regclass('public.loans') IS NOT NULL THEN
    EXECUTE 'CREATE INDEX IF NOT EXISTS idx_loans_user_status ON public.loans(user_id, status)';
  END IF;
  IF to_regclass('public.loan_repayment_schedule') IS NOT NULL THEN
    EXECUTE 'CREATE INDEX IF NOT EXISTS idx_schedule_loan ON public.loan_repayment_schedule(loan_id)';
  END IF;
  IF to_regclass('public.transactions') IS NOT NULL THEN
    EXECUTE 'CREATE INDEX IF NOT EXISTS idx_tx_user ON public.transactions(user_id)';
    EXECUTE 'CREATE INDEX IF NOT EXISTS idx_tx_loan ON public.transactions(loan_id)';
  END IF;
  IF to_regclass('public.notifications') IS NOT NULL THEN
    EXECUTE 'CREATE INDEX IF NOT EXISTS idx_notif_user ON public.notifications(user_id, is_read)';
  END IF;
  IF to_regclass('public.linked_accounts') IS NOT NULL THEN
    EXECUTE 'CREATE INDEX IF NOT EXISTS idx_linked_user ON public.linked_accounts(user_id)';
  END IF;
  IF to_regclass('public.login_events') IS NOT NULL THEN
    EXECUTE 'CREATE INDEX IF NOT EXISTS idx_login_user ON public.login_events(user_id)';
  END IF;
  IF to_regclass('public.credit_events') IS NOT NULL THEN
    EXECUTE 'CREATE INDEX IF NOT EXISTS idx_credit_user ON public.credit_events(user_id)';
  END IF;
END $$;

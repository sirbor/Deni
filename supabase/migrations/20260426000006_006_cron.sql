CREATE EXTENSION IF NOT EXISTS pg_cron;

SELECT cron.unschedule(jobid)
FROM cron.job
WHERE jobname = 'deni-check-overdue';

SELECT cron.schedule('deni-check-overdue', '0 8 * * *', $$
  UPDATE public.loans
  SET status = 'OVERDUE',
      penalty_kes = penalty_kes + ROUND(outstanding_kes * 0.01, 2)
  WHERE status = 'ACTIVE'
    AND due_at::date < CURRENT_DATE
    AND outstanding_kes > 0;

  UPDATE public.loan_repayment_schedule
  SET status = 'OVERDUE'
  WHERE status = 'UPCOMING'
    AND due_at::date < CURRENT_DATE;

  UPDATE public.users u
  SET credit_score = GREATEST(credit_score - 40, 0),
      loyalty_tier = public.compute_tier(GREATEST(credit_score - 40, 0)),
      loan_limit = public.compute_limit(GREATEST(credit_score - 40, 0))
  FROM public.loans l
  WHERE l.user_id = u.id
    AND l.status = 'OVERDUE'
    AND l.due_at::date = CURRENT_DATE - 1;

  INSERT INTO public.notifications(user_id, notif_type, title, body, icon_code, ref_id)
  SELECT l.user_id, 'REMINDER', 'Overdue Payment',
         'Your loan of KES ' || l.principal_kes || ' is overdue. Pay now to avoid penalties.',
         'WARN', l.id::text
  FROM public.loans l
  WHERE l.status = 'OVERDUE'
    AND l.due_at::date = CURRENT_DATE - 1;

  INSERT INTO public.notifications(user_id, notif_type, title, body, icon_code, ref_id)
  SELECT l.user_id, 'REMINDER', 'Payment Due Soon',
         'KES ' || s.emi_amount_kes || ' due in 7 days. Tap to pay.',
         'REM', l.id::text
  FROM public.loans l
  JOIN public.loan_repayment_schedule s ON s.loan_id = l.id
  WHERE s.due_at::date = CURRENT_DATE + 7
    AND s.status = 'UPCOMING';
$$);

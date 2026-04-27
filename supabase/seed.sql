-- Seed for local development only.
-- Creates Dominic Bor in auth.users first, then enriches public profile.

DO $$
DECLARE
  v_uid UUID := '11111111-1111-1111-1111-111111111111';
BEGIN
  IF NOT EXISTS (SELECT 1 FROM auth.users WHERE id = v_uid) THEN
    INSERT INTO auth.users (
      id, instance_id, aud, role, email, encrypted_password,
      email_confirmed_at, raw_app_meta_data, raw_user_meta_data,
      created_at, updated_at, confirmation_token, email_change_token_new,
      recovery_token
    ) VALUES (
      v_uid, '00000000-0000-0000-0000-000000000000', 'authenticated', 'authenticated',
      'sirbor@deni.app', crypt('local-dev-password', gen_salt('bf')),
      now(), '{"provider":"email","providers":["email"]}', '{"first_name":"Dominic","last_name":"Bor","auth_provider":"phone"}',
      now(), now(), '', '', ''
    );
  END IF;
END $$;

UPDATE public.users
SET
  first_name = 'Dominic',
  last_name = 'Bor',
  full_name = 'Dominic Bor',
  email = 'sirbor@deni.app',
  phone_e164 = '+254712345678',
  pin_hash = '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4',
  auth_provider = 'phone',
  credit_score = 720,
  loan_limit = 25000,
  balance_kes = 0,
  loyalty_tier = 'SILVER',
  kyc_status = 'VERIFIED',
  is_active = TRUE
WHERE id = '11111111-1111-1111-1111-111111111111';

INSERT INTO public.user_preferences(user_id, dark_mode, reminders, biometrics_enabled, app_lock_timeout_minutes)
VALUES ('11111111-1111-1111-1111-111111111111', FALSE, TRUE, TRUE, 5)
ON CONFLICT (user_id) DO UPDATE
SET dark_mode = FALSE, reminders = TRUE, biometrics_enabled = TRUE, app_lock_timeout_minutes = 5;

ALTER TABLE public.loans DISABLE TRIGGER trg_after_loan_insert;
ALTER TABLE public.transactions DISABLE TRIGGER trg_after_transaction_insert;

INSERT INTO public.loans (id, user_id, reference_number, loan_type, principal_kes, interest_rate, tenure_months, interest_kes, total_kes, outstanding_kes, disbursed_at, due_at, repaid_amount_kes, status, is_paid)
VALUES
  (1,'11111111-1111-1111-1111-111111111111','DENI12345678','Portfolio Loan',15000,0.1500,3,2250,17250,12250,now()-interval '30 days',now()+interval '60 days',5000,'ACTIVE',FALSE),
  (2,'11111111-1111-1111-1111-111111111111','LN-0002','Emergency Loan',5000,0.1500,1,750,5750,5750,now()-interval '75 days',now()-interval '45 days',0,'OVERDUE',FALSE),
  (3,'11111111-1111-1111-1111-111111111111','LN-0003','Personal Loan',20000,0.1500,3,3000,23000,0,now()-interval '140 days',now()-interval '50 days',23000,'PAID',TRUE),
  (4,'11111111-1111-1111-1111-111111111111','LN-0004','Business Loan',30000,0.1500,6,4500,34500,0,now()-interval '145 days',now()+interval '35 days',34500,'PAID',TRUE),
  (5,'11111111-1111-1111-1111-111111111111','LN-0005','Personal Loan',10000,0.1500,1,1500,11500,0,now()-interval '250 days',now()-interval '220 days',11500,'PAID',TRUE),
  (6,'11111111-1111-1111-1111-111111111111','LN-0006','Personal Loan',5000,0.1500,3,750,5750,0,now()-interval '320 days',now()-interval '230 days',5750,'PAID',TRUE),
  (7,'11111111-1111-1111-1111-111111111111','LN-0007','Emergency Loan',2500,0.1500,1,375,2875,0,now()-interval '395 days',now()-interval '365 days',2875,'PAID',TRUE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.loan_repayment_schedule (loan_id, installment_number, due_at, emi_amount_kes, principal_component_kes, interest_component_kes, balance_after_kes, paid_amount_kes, status)
VALUES
  (1,1,now()+interval '5 days',5750,5000,750,10000,5750,'PAID'),
  (1,2,now()+interval '36 days',5750,5000,750,5000,0,'UPCOMING'),
  (1,3,now()+interval '66 days',5750,5000,750,0,0,'UPCOMING')
ON CONFLICT (loan_id, installment_number) DO NOTHING;

INSERT INTO public.transactions (loan_id, user_id, title, amount_kes, tx_type, status, source, occurred_at)
VALUES
  (1,'11111111-1111-1111-1111-111111111111','Loan disbursement',15000,'CREDIT','ACTIVE','SYSTEM',now()-interval '30 days'),
  (1,'11111111-1111-1111-1111-111111111111','M-Pesa repayment',5000,'DEBIT','ACTIVE','MPESA',now()-interval '15 days'),
  (2,'11111111-1111-1111-1111-111111111111','Loan repayment completed',9000,'DEBIT','PAID','MPESA',now()-interval '75 days'),
  (3,'11111111-1111-1111-1111-111111111111','Late repayment warning',12000,'DEBIT','OVERDUE','SYSTEM',now()-interval '110 days'),
  (4,'11111111-1111-1111-1111-111111111111','Loan disbursement',20000,'CREDIT','PAID','SYSTEM',now()-interval '140 days');

ALTER TABLE public.loans ENABLE TRIGGER trg_after_loan_insert;
ALTER TABLE public.transactions ENABLE TRIGGER trg_after_transaction_insert;

INSERT INTO public.notifications (user_id, notif_type, title, body, icon_code, is_read, created_at)
VALUES
  ('11111111-1111-1111-1111-111111111111','APPROVAL','Loan Approved','Your KES 15,000 loan has been disbursed to your M-Pesa.','APP',FALSE,now()-interval '1 hour'),
  ('11111111-1111-1111-1111-111111111111','REMINDER','Payment Due Soon','KES 5,750 due in 7 days. Tap to pay.','REM',FALSE,now()-interval '2 hours'),
  ('11111111-1111-1111-1111-111111111111','OFFER','New Offer Available','You now qualify for KES 75,000. Apply now.','OFF',TRUE,now()-interval '1 day'),
  ('11111111-1111-1111-1111-111111111111','REPAYMENT','Payment Received','KES 6,835 received. Personal Loan balance updated.','PAY',TRUE,now()-interval '50 days'),
  ('11111111-1111-1111-1111-111111111111','SYSTEM','Credit Score Updated','Your score improved by 12 points to 732. Keep it up!','SYS',TRUE,now()-interval '55 days'),
  ('11111111-1111-1111-1111-111111111111','REMINDER','Overdue Payment','Emergency Loan KES 5,750 overdue. Pay now to avoid penalty.','WARN',TRUE,now()-interval '70 days'),
  ('11111111-1111-1111-1111-111111111111','REPAYMENT','Payment Received','KES 5,214 received. Business Loan balance updated.','PAY',TRUE,now()-interval '77 days'),
  ('11111111-1111-1111-1111-111111111111','OFFER','Loyalty Reward Unlocked','You''ve reached Silver tier! Enjoy lower interest rates.','TIER',TRUE,now()-interval '100 days'),
  ('11111111-1111-1111-1111-111111111111','SYSTEM','Profile Verified','Your ID has been successfully verified.','ID',TRUE,now()-interval '105 days'),
  ('11111111-1111-1111-1111-111111111111','APPROVAL','Welcome to Deni','Your account is ready. Apply for your first loan.','NEW',TRUE,now()-interval '110 days');

INSERT INTO public.linked_accounts (user_id, provider, account_label, masked_number, is_primary)
VALUES
  ('11111111-1111-1111-1111-111111111111','M-Pesa','Primary Wallet','07** *** 678',TRUE),
  ('11111111-1111-1111-1111-111111111111','NCBA','Salary Account','**** 9821',FALSE),
  ('11111111-1111-1111-1111-111111111111','VISA','Debit Card','**** 4407',FALSE);

INSERT INTO public.login_events (user_id, device, location, is_current, logged_in_at)
VALUES
  ('11111111-1111-1111-1111-111111111111','Samsung Galaxy A54','Nairobi, KE',TRUE,now()-interval '30 minutes'),
  ('11111111-1111-1111-1111-111111111111','Samsung Galaxy A54','Nairobi, KE',FALSE,now()-interval '1 day'),
  ('11111111-1111-1111-1111-111111111111','Samsung Galaxy A54','Thika, KE',FALSE,now()-interval '4 days');

INSERT INTO public.credit_events (user_id, event_type, score_delta, score_before, score_after, description, created_at)
VALUES
  ('11111111-1111-1111-1111-111111111111','REPAY_ONTIME',15,705,720,'On-time repayment — Portfolio Loan',now()-interval '15 days'),
  ('11111111-1111-1111-1111-111111111111','REPAY_EARLY',25,680,705,'Early repayment — Business Loan',now()-interval '77 days'),
  ('11111111-1111-1111-1111-111111111111','REPAY_LATE',-20,700,680,'Late repayment — Emergency Loan',now()-interval '110 days'),
  ('11111111-1111-1111-1111-111111111111','KYC_VERIFIED',20,500,520,'Identity verified',now()-interval '105 days');

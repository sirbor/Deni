-- Align payment_intents.loan_id with loans.id UUID schema.

DO $$
DECLARE
  v_type TEXT;
BEGIN
  SELECT data_type
  INTO v_type
  FROM information_schema.columns
  WHERE table_schema = 'public'
    AND table_name = 'payment_intents'
    AND column_name = 'loan_id';

  IF v_type IS NULL THEN
    ALTER TABLE public.payment_intents
      ADD COLUMN loan_id UUID;
  ELSIF v_type <> 'uuid' THEN
    ALTER TABLE public.payment_intents
      ALTER COLUMN loan_id TYPE UUID
      USING NULL;
  END IF;
END $$;

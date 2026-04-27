-- Centralized loan pricing configuration used by edge functions.

CREATE TABLE IF NOT EXISTS public.loan_pricing_config (
  tenure_days INT PRIMARY KEY,
  interest_rate NUMERIC(10,6) NOT NULL,
  processing_fee_rate NUMERIC(10,6) NOT NULL DEFAULT 0.03,
  is_active BOOLEAN NOT NULL DEFAULT true,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO public.loan_pricing_config (tenure_days, interest_rate, processing_fee_rate, is_active)
VALUES
  (14, 0.046667, 0.03, true),
  (30, 0.100000, 0.03, true),
  (45, 0.150000, 0.03, true),
  (60, 0.200000, 0.03, true)
ON CONFLICT (tenure_days) DO UPDATE
SET
  interest_rate = EXCLUDED.interest_rate,
  processing_fee_rate = EXCLUDED.processing_fee_rate,
  is_active = EXCLUDED.is_active,
  updated_at = now();

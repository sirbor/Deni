-- Reconcile loan repaid balances from recorded repayment transactions.
-- This heals loans affected before RLS update permissions were fixed.

WITH tx_sums AS (
  SELECT
    t.loan_id,
    SUM(t.amount_kes)::numeric AS debit_sum
  FROM public.transactions t
  WHERE t.loan_id IS NOT NULL
    AND UPPER(COALESCE(t.tx_type, '')) = 'DEBIT'
  GROUP BY t.loan_id
),
calc AS (
  SELECT
    l.id,
    (COALESCE(l.principal_kes, l.amount, 0)::numeric) AS principal,
    COALESCE(l.interest_rate, 0)::numeric AS rate,
    GREATEST(
      COALESCE(l.repaid_amount_kes, l.amount_repaid, 0)::numeric,
      COALESCE(ts.debit_sum, 0)::numeric
    ) AS raw_repaid,
    l.due_at
  FROM public.loans l
  LEFT JOIN tx_sums ts ON ts.loan_id = l.id
),
normalized AS (
  SELECT
    id,
    (principal + (principal * rate)) AS total_due,
    LEAST(raw_repaid, (principal + (principal * rate))) AS repaid_norm,
    due_at
  FROM calc
)
UPDATE public.loans l
SET
  repaid_amount_kes = n.repaid_norm,
  amount_repaid = n.repaid_norm,
  status = CASE
    WHEN n.repaid_norm >= (n.total_due - 1) THEN 'PAID'::loan_status
    WHEN n.due_at IS NOT NULL AND n.due_at < now() THEN 'OVERDUE'::loan_status
    ELSE 'ACTIVE'::loan_status
  END,
  updated_at = now()
FROM normalized n
WHERE l.id = n.id;

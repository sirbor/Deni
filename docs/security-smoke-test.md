# Security Smoke Test (Supabase Functions)

This document validates the newly deployed security controls:

- Sign-in lockouts (5 failed attempts / 15 minutes)
- Idempotency key behavior
- Repayment webhook settlement flow

Project ref used below: `gigxwidiwfteigolfpma`

Base URL:

`https://gigxwidiwfteigolfpma.supabase.co/functions/v1`

## 1) Register a disposable test user

```bash
BASE_URL="https://gigxwidiwfteigolfpma.supabase.co/functions/v1"
PHONE="+2547$(shuf -i 10000000-99999999 -n 1)"
PIN="1234"

REGISTER_RESP=$(curl -sS -X POST "$BASE_URL/register" \
  -H "Content-Type: application/json" \
  -d "{\"firstName\":\"Sec\",\"lastName\":\"Test\",\"phone\":\"$PHONE\",\"pin\":\"$PIN\"}")

echo "$REGISTER_RESP"
ACCESS_TOKEN=$(echo "$REGISTER_RESP" | jq -r '.accessToken')
```

Expected:
- `ok: true`
- non-empty `accessToken`

## 2) Validate lockout after failed PIN attempts

```bash
for i in 1 2 3 4 5; do
  echo "Wrong attempt $i"
  curl -sS -X POST "$BASE_URL/sign-in" \
    -H "Content-Type: application/json" \
    -d "{\"phone\":\"$PHONE\",\"pin\":\"0000\"}" | jq
done

echo "Attempt 6 (should be locked):"
curl -i -sS -X POST "$BASE_URL/sign-in" \
  -H "Content-Type: application/json" \
  -d "{\"phone\":\"$PHONE\",\"pin\":\"0000\"}"
```

Expected:
- first 5 attempts: `401 Invalid phone/PIN combination`
- 6th attempt: `429 Too many attempts. Try again later.`

## 3) Verify idempotency key on apply-loan

Note: this requires a KYC-verified profile and sufficient limit, so if it returns 403, first update test profile in DB.

```bash
IDEMP_KEY="idem-apply-$(date +%s)"
BODY='{"amountKes":5000,"tenureMonths":3,"loanType":"Personal Loan","idempotencyKey":"'"$IDEMP_KEY"'"}'

curl -sS -X POST "$BASE_URL/apply-loan" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Idempotency-Key: $IDEMP_KEY" \
  -H "Content-Type: application/json" \
  -d "$BODY" | jq

curl -sS -X POST "$BASE_URL/apply-loan" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Idempotency-Key: $IDEMP_KEY" \
  -H "Content-Type: application/json" \
  -d "$BODY" | jq
```

Expected:
- same response payload on repeated request with same key.

## 4) Validate repay intent + webhook settlement

1) Create repayment intent:

```bash
REPAY_KEY="idem-repay-$(date +%s)"
REPAY_RESP=$(curl -sS -X POST "$BASE_URL/repay-loan" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Idempotency-Key: $REPAY_KEY" \
  -H "Content-Type: application/json" \
  -d '{"loanId":1,"amountKes":1000,"source":"MPESA","idempotencyKey":"'"$REPAY_KEY"'"}')

echo "$REPAY_RESP" | jq
INTENT_ID=$(echo "$REPAY_RESP" | jq -r '.paymentIntent.id')
```

Expected:
- returns `paymentIntent.status = PENDING_VERIFICATION`

2) Settle via webhook:

```bash
WEBHOOK_SECRET="<PAYMENT_WEBHOOK_SECRET>"

curl -sS -X POST "$BASE_URL/repayment-webhook" \
  -H "x-webhook-secret: $WEBHOOK_SECRET" \
  -H "Content-Type: application/json" \
  -d '{"paymentIntentId":"'"$INTENT_ID"'","providerReference":"daraja-test-001","settledAmountKes":1000,"settled":true}' | jq
```

Expected:
- `ok: true`, `settled: true`
- loan outstanding reduced and status updated

## 5) Audit-log check

Confirm new rows exist for:
- `auth.register`
- `auth.signin_failed` / `auth.signin_success`
- `loan.apply`
- `loan.repay_intent_created`
- `loan.repay_webhook_settled`

Use Supabase SQL editor:

```sql
select action, user_id, created_at
from public.audit_logs
order by created_at desc
limit 50;
```


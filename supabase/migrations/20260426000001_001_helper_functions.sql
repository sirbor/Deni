CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE OR REPLACE FUNCTION public.compute_tier(score int)
RETURNS text
LANGUAGE plpgsql
IMMUTABLE
AS $$
BEGIN
  RETURN CASE
    WHEN score >= 750 THEN 'PLATINUM'
    WHEN score >= 500 THEN 'GOLD'
    WHEN score >= 300 THEN 'SILVER'
    ELSE 'BRONZE'
  END;
END;
$$;

CREATE OR REPLACE FUNCTION public.compute_limit(score int)
RETURNS numeric
LANGUAGE plpgsql
IMMUTABLE
AS $$
BEGIN
  RETURN CASE
    WHEN score >= 750 THEN 50000
    WHEN score >= 500 THEN 40000
    WHEN score >= 300 THEN 25000
    ELSE 10000
  END;
END;
$$;

CREATE OR REPLACE FUNCTION public.update_updated_at()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$;

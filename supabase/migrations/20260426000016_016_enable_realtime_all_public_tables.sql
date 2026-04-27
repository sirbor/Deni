-- Enable Supabase Realtime for all current public tables.
-- This keeps the setting reproducible across environments.

DO $$
DECLARE
  r RECORD;
BEGIN
  FOR r IN
    SELECT tablename
    FROM pg_tables
    WHERE schemaname = 'public'
  LOOP
    -- Ensure UPDATE/DELETE payloads include full row data.
    BEGIN
      EXECUTE format('ALTER TABLE public.%I REPLICA IDENTITY FULL', r.tablename);
    EXCEPTION
      WHEN OTHERS THEN
        NULL;
    END;

    -- Add every public table to supabase_realtime publication.
    BEGIN
      EXECUTE format('ALTER PUBLICATION supabase_realtime ADD TABLE public.%I', r.tablename);
    EXCEPTION
      WHEN duplicate_object THEN
        NULL;
      WHEN OTHERS THEN
        NULL;
    END;
  END LOOP;
END $$;

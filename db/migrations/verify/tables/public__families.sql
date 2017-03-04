-- Verify family:tables/public__families on pg

SET client_min_messages TO 'warning';

BEGIN;

  SELECT family_id, name, created_at
  FROM public.families
  WHERE false;

ROLLBACK;

-- vim: expandtab shiftwidth=2

-- Revert family:tables/public__families from pg

SET client_min_messages TO 'warning';

BEGIN;

  DROP TABLE public.families CASCADE;

COMMIT;

-- vim: expandtab shiftwidth=2

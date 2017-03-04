-- Revert family:tables/mybank__families from pg

SET client_min_messages TO 'warning';

BEGIN;

  DROP TABLE mybank.families;

COMMIT;

-- vim: expandtab shiftwidth=2

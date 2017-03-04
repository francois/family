-- Revert family:schemas/mybank from pg

SET client_min_messages TO 'warning';

BEGIN;

  DROP SCHEMA mybank;

COMMIT;

-- vim: expandtab shiftwidth=2

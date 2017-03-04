-- Revert family:tables/mybank__goals from pg

SET client_min_messages TO 'warning';

BEGIN;

  DROP TABLE mybank.goals;

COMMIT;

-- vim: expandtab shiftwidth=2

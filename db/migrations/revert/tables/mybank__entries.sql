-- Revert family:tables/mybank__entries from pg

SET client_min_messages TO 'warning';

BEGIN;

  DROP TABLE mybank.entries;

COMMIT;

-- vim: expandtab shiftwidth=2

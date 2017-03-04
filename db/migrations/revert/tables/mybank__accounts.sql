-- Revert family:tables/mybank__accounts from pg

SET client_min_messages TO 'warning';

BEGIN;

  DROP TABLE mybank.accounts;

COMMIT;

-- vim: expandtab shiftwidth=2

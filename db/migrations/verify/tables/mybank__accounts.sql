-- Verify family:tables/mybank__accounts on pg

SET client_min_messages TO 'warning';

BEGIN;

  SELECT family_id, account_id, name, created_at
  FROM mybank.accounts
  WHERE false;

ROLLBACK;

-- vim: expandtab shiftwidth=2

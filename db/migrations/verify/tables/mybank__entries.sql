-- Verify family:tables/mybank__entries on pg

SET client_min_messages TO 'warning';

BEGIN;

  SELECT family_id, account_id, entry_id, posted_on, description, amount, created_at
  FROM mybank.entries
  WHERE false;

ROLLBACK;

-- vim: expandtab shiftwidth=2

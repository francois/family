-- Verify family:tables/mybank__families on pg

SET client_min_messages TO 'warning';

BEGIN;

  SELECT family_id, yearly_interest_rate
  FROM mybank.families
  WHERE false;

ROLLBACK;

-- vim: expandtab shiftwidth=2

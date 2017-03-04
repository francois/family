-- Verify family:tables/mybank__goals on pg

SET client_min_messages TO 'warning';

BEGIN;

  SELECT family_id, account_id, goal_id, description, due_on, target, created_at
  FROM mybank.goals
  WHERE false;

ROLLBACK;

-- vim: expandtab shiftwidth=2

-- Verify family:schemas/mybank on pg

SET client_min_messages TO 'warning';

BEGIN;

  SELECT has_schema_privilege('mybank', 'usage');

ROLLBACK;

-- vim: expandtab shiftwidth=2

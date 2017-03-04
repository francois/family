-- Verify family:extensions/uuid-ossp on pg

SET client_min_messages TO 'warning';

BEGIN;

  SELECT has_function_privilege('uuid_generate_v4()', 'execute');

ROLLBACK;

-- vim: expandtab shiftwidth=2

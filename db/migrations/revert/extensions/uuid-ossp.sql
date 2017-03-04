-- Revert family:extensions/uuid-ossp from pg

SET client_min_messages TO 'warning';

BEGIN;

  DROP EXTENSION "uuid-ossp";

COMMIT;

-- vim: expandtab shiftwidth=2

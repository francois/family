-- Deploy family:extensions/uuid-ossp to pg

SET client_min_messages TO 'warning';

BEGIN;

  CREATE EXTENSION "uuid-ossp";

COMMIT;

-- vim: expandtab shiftwidth=2

-- Deploy family:schemas/mybank to pg

SET client_min_messages TO 'warning';

BEGIN;

  CREATE SCHEMA mybank;

COMMIT;

-- vim: expandtab shiftwidth=2

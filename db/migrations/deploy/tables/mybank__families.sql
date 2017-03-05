-- Deploy family:tables/mybank__families to pg
-- requires: schemas/mybank

SET client_min_messages TO 'warning';

BEGIN;

  CREATE TABLE mybank.families(
      family_id uuid not null primary key
    , locale text not null check(trim(locale) = locale and length(locale) > 0)
    , yearly_interest_rate decimal(5, 2) not null default 10 check(yearly_interest_rate between 0.0 and 100.0)
  );

COMMIT;

-- vim: expandtab shiftwidth=2

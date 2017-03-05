-- Deploy family:tables/mybank__accounts to pg
-- requires: tables/mybank__families

SET client_min_messages TO 'warning';

BEGIN;

  CREATE TABLE mybank.accounts(
      family_id uuid not null
    , account_id uuid not null
    , name text not null check(length(name) > 0 and trim(name) = name)
    , salary numeric(6, 2) not null check(salary between 0 and 9999.99)
    , created_at timestamp with time zone not null default current_timestamp

    , primary key(family_id, account_id)
    , foreign key(family_id) references mybank.families on update cascade on delete cascade
  );

  COMMENT ON COLUMN mybank.accounts.salary IS 'Represents the salary of this person, per UNIT. UNIT is left at the discretion of the human people outside the system. UNIT could be chores, minutes, etc.';

COMMIT;

-- vim: expandtab shiftwidth=2

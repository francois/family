-- Deploy family:tables/mybank__accounts to pg
-- requires: tables/mybank__families

SET client_min_messages TO 'warning';

BEGIN;

  CREATE TABLE mybank.accounts(
      family_id uuid not null
    , account_id uuid not null
    , name text not null check(length(name) > 0 and trim(name) = name)
    , created_at timestamp with time zone not null default current_timestamp

    , primary key(family_id, account_id)
    , foreign key(family_id) references mybank.families on update cascade on delete cascade
  );

COMMIT;

-- vim: expandtab shiftwidth=2

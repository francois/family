-- Deploy family:tables/mybank__entries to pg
-- requires: tables/mybank__accounts

SET client_min_messages TO 'warning';

BEGIN;

  CREATE TABLE mybank.entries(
      family_id uuid
    , account_id uuid
    , entry_id uuid not null default uuid_generate_v4()
    , posted_on date not null
    , description text not null check(length(description) > 0 and trim(description) = description)
    , amount decimal(9, 2) not null
    , created_at timestamp with time zone not null default current_timestamp

    , primary key(family_id, account_id, entry_id)
    , foreign key(family_id, account_id) references mybank.accounts on update cascade on delete cascade
  );

  CREATE INDEX index_entries_on_posted_on ON mybank.entries(family_id, account_id, posted_on, created_at);

COMMIT;

-- vim: expandtab shiftwidth=2

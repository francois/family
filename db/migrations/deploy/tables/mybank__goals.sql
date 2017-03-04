-- Deploy family:tables/mybank__goals to pg
-- requires: tables/mybank__accounts

SET client_min_messages TO 'warning';

BEGIN;

  CREATE TABLE mybank.goals(
      family_id uuid not null
    , account_id uuid not null
    , goal_id uuid not null default uuid_generate_v4()
    , description text not null check(length(description) > 0 and trim(description) = description)
    , due_on date not null
    , target decimal(9, 2) not null check(target > 0)
    , created_at timestamp with time zone not null default current_timestamp

    , primary key(family_id, account_id, goal_id)
    , foreign key(family_id, account_id) references mybank.accounts on update cascade on delete cascade
  );

  CREATE INDEX index_goals_on_due_on ON mybank.goals(family_id, account_id, due_on, created_at);

COMMIT;

-- vim: expandtab shiftwidth=2

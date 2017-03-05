-- Deploy family:tables/public__families to pg

SET client_min_messages TO 'warning';

BEGIN;

  CREATE TABLE public.families(
      family_id uuid primary key
    , name text not null check(trim(name) = name and length(name) > 0)
    , locale text not null check(trim(locale) = locale and length(locale) > 0)
    , created_at timestamp with time zone default now() not null
  );

COMMIT;

-- vim: expandtab shiftwidth=2

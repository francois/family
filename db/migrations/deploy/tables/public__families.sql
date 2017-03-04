-- Deploy family:tables/public__families to pg

SET client_min_messages TO 'warning';

BEGIN;

  CREATE TABLE public.families(
    family_id uuid not null,
    name text not null,
    created_at timestamp with time zone default now() not null,
    constraint families_name_check check (trim(name) = name and length(name) > 0)
  );

COMMIT;

-- vim: expandtab shiftwidth=2

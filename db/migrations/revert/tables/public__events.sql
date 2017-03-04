-- Revert family:tables/public__events from pg

SET client_min_messages TO 'warning';

BEGIN;

  DROP TABLE public.events;
  DROP TABLE public.event_types;

COMMIT;

-- vim: expandtab shiftwidth=2

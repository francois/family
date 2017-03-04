-- Verify family:tables/public__events on pg

SET client_min_messages TO 'warning';

BEGIN;

  SELECT event_type
  FROM public.event_types
  WHERE false;

  SELECT event_id, event_seq, event_type, posted_at, contents
  FROM public.events
  WHERE false;

ROLLBACK;

-- vim: expandtab shiftwidth=2

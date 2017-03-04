-- Deploy family:tables/public__events to pg
-- requires: extensions/uuid-ossp

SET client_min_messages TO 'warning';

BEGIN;

  CREATE TABLE public.event_types(
    event_type text not null primary key
  );

  CREATE TABLE public.events(
      event_id uuid primary key default uuid_generate_v4()
    , event_seq bigserial not null unique
    , posted_at timestamp with time zone not null default current_timestamp
    , event_type text not null references public.event_types on update cascade on delete restrict
    , contents jsonb not null
  );

  COMMENT ON COLUMN public.events.event_seq IS 'The sequence number of this event. All events are ordered in time and space using this sequence number. When looking globally, there may be gaps between different events, but the total order remains.';
  COMMENT ON COLUMN public.events.posted_at IS 'The instant when the database server stored this event. This may or may not be different from the moment when the application generated the event.';
  COMMENT ON COLUMN public.events.contents IS 'A JSON representation of the event''s data attributes.' ;

COMMIT;

-- vim: expandtab shiftwidth=2

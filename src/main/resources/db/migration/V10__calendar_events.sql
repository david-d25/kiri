create table main.calendar_events (
    id              uuid primary key,
    title           text not null,
    description     text,
    timezone        text not null default 'UTC',
    fires_at        timestamp with time zone,
    rrule           text,
    dtstart         timestamp with time zone,
    exdates         timestamp with time zone[] not null default '{}',
    next_fire_at    timestamp with time zone,
    last_fired_at   timestamp with time zone,
    wake_agent      boolean not null default true,
    missed_policy   text not null default 'FIRE_ONCE',
    enabled         boolean not null default true,
    created_at      timestamp with time zone not null default now(),
    updated_at      timestamp with time zone not null default now(),
    -- exactly one of (fires_at) or (rrule + dtstart) must be set
    check (
        (fires_at is not null and rrule is null and dtstart is null) or
        (fires_at is null and rrule is not null and dtstart is not null)
    ),
    check (missed_policy in ('FIRE_ONCE', 'SKIP'))
);

create index idx_calendar_events__due
    on main.calendar_events (next_fire_at)
    where enabled and next_fire_at is not null;

create index idx_calendar_events__created_at
    on main.calendar_events (created_at desc);

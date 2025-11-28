create table main.settings (
    key text primary key,
    value text,
    encrypted boolean not null default false,
    updated_at timestamp with time zone not null default now()
);
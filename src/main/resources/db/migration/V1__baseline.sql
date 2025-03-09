create table telegram_chats (
    id bigint primary key
);

create table user_sessions (
    id                      uuid primary key,
    user_id                 bigint not null,
    session_token_encrypted bytea not null,
    valid_until             timestamp with time zone not null
);
create index user_sessions_user_id_idx on user_sessions (user_id);
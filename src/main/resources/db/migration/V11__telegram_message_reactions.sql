-- Reactions are delivered as separate updates (message_reaction / message_reaction_count) independent of the
-- message itself, so they are stored in their own table keyed by (chat_id, message_id). This keeps the heavy
-- telegram.messages save path untouched and avoids re-saving the whole message graph (or wiping reactions on
-- message edits) just to record a reaction change.
-- `reactions` holds a JSON array of reactions serialized by the application (kept as text to avoid coupling to
-- Hibernate's JSON type mapping); it is never queried by content.
create table telegram.message_reactions (
    chat_id    bigint not null,
    message_id integer not null,
    reactions  text not null default '[]',
    updated_at timestamp with time zone not null default now(),
    primary key (chat_id, message_id)
);

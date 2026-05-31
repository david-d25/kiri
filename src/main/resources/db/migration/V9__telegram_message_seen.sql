-- Per-message `seen` flag replaces the chat-level `last_read_message_id` cursor.
-- Backfill existing rows as seen so the agent doesn't see history as unread after deploy.
alter table telegram.messages add column seen boolean not null default false;
update telegram.messages set seen = true;
create index idx_telegram_messages__chat_id_seen on telegram.messages (chat_id, seen);

alter table main.telegram_chat_metadata drop column last_read_message_id cascade;

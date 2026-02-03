create extension if not exists pg_trgm; -- for text search

create index idx_telegram_messages__chat_id on telegram.messages (chat_id);
create index idx_telegram_messages__message_id on telegram.messages (message_id);
create index idx_telegram_messages__chat_id_message_id on telegram.messages (chat_id, message_id);
create index idx_telegram_messages__message_thread_id on telegram.messages (message_thread_id);
create index idx_telegram_messages__date on telegram.messages (date);
create index idx_telegram_messages__edit_date on telegram.messages (edit_date);
create index idx_telegram_messages__from_id on telegram.messages (from_id);
create index idx_telegram_messages__forward_origin_id on telegram.messages (forward_origin_id);
create index idx_telegram_messages__reply_to_message__chat_id on telegram.messages (reply_to_message__chat_id);
create index idx_telegram_messages__reply_to_message__message_id on telegram.messages (reply_to_message__message_id);
create index idx_telegram_messages__text on telegram.messages using gin (text main.gin_trgm_ops);
create index idx_telegram_messages__caption on telegram.messages using gin (caption main.gin_trgm_ops);
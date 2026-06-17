-- Rich messages (Bot API 10.1) carry a structured block tree (rich_message) instead of flat text + entities.
-- We don't model the full ~49-type tree; instead, for messages the bot sends via sendRichMessage we store the
-- source HTML here. A non-null value marks the message as a rich message (its `text` stays null), so it is never
-- confused with a normal message and is rendered accordingly.
alter table telegram.messages add column rich_message text;

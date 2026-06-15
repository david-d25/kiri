-- Per-voter selections for non-anonymous polls. Telegram delivers live votes on non-anonymous polls only as
-- `poll_answer` updates (one per voter), not as aggregated `poll` updates, so the bot must aggregate counts itself.
-- Storing each voter's current selection lets us compute correct deltas when a voter changes or retracts a vote.
create table telegram.poll_answers (
    poll_id    text not null,
    voter_id   bigint not null,
    option_ids integer[] not null default '{}',
    updated_at timestamp with time zone not null default now(),
    primary key (poll_id, voter_id)
);

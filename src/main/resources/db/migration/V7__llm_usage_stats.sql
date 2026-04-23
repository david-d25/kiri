create table main.llm_usage_stats (
    id                           uuid primary key,
    timestamp                    timestamp with time zone not null,
    provider                     text not null,
    model                        text not null,
    input_tokens                 bigint not null default 0,
    output_tokens                bigint not null default 0,
    cache_read_input_tokens      bigint not null default 0,
    cache_creation_input_tokens  bigint not null default 0,
    duration_ms                  bigint not null default 0
);

create index idx_llm_usage_stats__timestamp on main.llm_usage_stats (timestamp desc);
create index idx_llm_usage_stats__model_timestamp on main.llm_usage_stats (model, timestamp desc);

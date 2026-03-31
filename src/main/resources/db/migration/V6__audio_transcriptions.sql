create table telegram.audio_transcriptions (
    file_unique_id text primary key,
    text text not null,
    created_at timestamp with time zone not null default now()
);

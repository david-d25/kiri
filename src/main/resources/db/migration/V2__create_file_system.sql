create schema if not exists filesystem;

create table if not exists filesystem.spaces (
    id uuid primary key,
    slug varchar(128) not null,
    display_name varchar(255) not null,
    description text,
    owner_user_id bigint,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create unique index if not exists idx_file_system_spaces__slug__owner
    on filesystem.spaces (LOWER(slug), owner_user_id);

create table if not exists filesystem.nodes (
    id uuid primary key,
    space_id uuid not null references filesystem.spaces (id) on delete cascade,
    parent_id uuid references filesystem.nodes (id) on delete cascade,
    name text not null,
    type text not null,
    mime_type text,
    size bigint not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create unique index if not exists idx_file_system_nodes_unique_name
    on filesystem.nodes (
        space_id,
        COALESCE(parent_id, '00000000-0000-0000-0000-000000000000'::uuid),
        LOWER(name)
    );

create table if not exists filesystem.node_attributes (
    node_id uuid not null references filesystem.nodes (id) on delete cascade,
    attribute_key text not null,
    attribute_value text,
    primary key (node_id, attribute_key)
);

create table if not exists filesystem.file_contents (
    node_id uuid primary key references filesystem.nodes (id) on delete cascade,
    content bytea not null
);

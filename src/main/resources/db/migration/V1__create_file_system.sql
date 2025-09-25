CREATE TABLE IF NOT EXISTS main.file_system_spaces (
    id UUID PRIMARY KEY,
    slug VARCHAR(128) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    description TEXT,
    owner_user_id BIGINT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS file_system_spaces_slug_owner_unique
    ON main.file_system_spaces (LOWER(slug), owner_user_id);

CREATE TABLE IF NOT EXISTS main.file_system_nodes (
    id UUID PRIMARY KEY,
    space_id UUID NOT NULL REFERENCES main.file_system_spaces (id) ON DELETE CASCADE,
    parent_id UUID REFERENCES main.file_system_nodes (id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(32) NOT NULL,
    mime_type VARCHAR(255),
    size BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS file_system_nodes_unique_name
    ON main.file_system_nodes (
        space_id,
        COALESCE(parent_id, '00000000-0000-0000-0000-000000000000'::uuid),
        LOWER(name)
    );

CREATE TABLE IF NOT EXISTS main.file_system_node_attributes (
    node_id UUID NOT NULL REFERENCES main.file_system_nodes (id) ON DELETE CASCADE,
    attribute_key VARCHAR(128) NOT NULL,
    attribute_value TEXT,
    PRIMARY KEY (node_id, attribute_key)
);

CREATE TABLE IF NOT EXISTS main.file_system_file_contents (
    node_id UUID PRIMARY KEY REFERENCES main.file_system_nodes (id) ON DELETE CASCADE,
    content BYTEA NOT NULL
);

drop index filesystem.idx_file_system_nodes_unique_name;

create unique index idx_file_system_nodes_unique_name on filesystem.nodes (
    coalesce(parent_id, '00000000-0000-0000-0000-000000000000'::uuid),
    name
);

alter table filesystem.nodes drop column space_id;
alter table filesystem.nodes drop column mime_type;

drop index filesystem.idx_file_system_spaces__slug__owner;
drop table filesystem.spaces;

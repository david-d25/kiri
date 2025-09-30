export type FileSystemCreateDirectoryRequest = {
    parentId: string | null;
    name: string;
    attributes: { [ key: string ]: string }
}
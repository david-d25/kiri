export type FileSystemCreateFileRequest = {
    parentId: string | null;
    name: string;
    mimeType: string | null;
    attributes: { [ key: string ]: string };
    contentBase64: string;
};
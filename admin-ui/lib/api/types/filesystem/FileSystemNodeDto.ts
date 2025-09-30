export type FileSystemNodeDto = {
    id: string;
    spaceId: string;
    parentId: string | null;
    name: string;
    type: FileSystemNodeDto.Type;
    mimeType: string;
    size: number;
    attributes: { [ ket: string ]: string };
    createdAt: number;
    updatedAt: number;
}

export namespace FileSystemNodeDto {
    export type Type = "directory" | "file";
}
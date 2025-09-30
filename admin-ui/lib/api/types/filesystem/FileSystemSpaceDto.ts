export type FileSystemSpaceDto = {
    id: string;
    slug: string;
    displayName: string;
    description: string | null;
    ownerUserId: string | null;
    createdAt: Date;
    updatedAt: Date;
}
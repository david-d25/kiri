export type FileSystemCreateSpaceRequest = {
    slug: string;
    displayName: string;
    description: string | null;
    ownerUserId: string | null;
}
export type TelegramChatMetadataDto = {
    lastReadMessageId: number | null;
    notificationMode: TelegramChatMetadataDto.NotificationMode;
    mutedUntil: number | null;
    archived: boolean;
    pinned: boolean;
    enabled: boolean;
};

export namespace TelegramChatMetadataDto {
    export type NotificationMode = "ALL" | "ONLY_MENTIONS" | "NONE";
}
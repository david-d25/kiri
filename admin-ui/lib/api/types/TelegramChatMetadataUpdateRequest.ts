import {TelegramChatMetadataDto} from "@/lib/api/types/TelegramChatMetadataDto";

export type TelegramChatMetadataUpdateRequest = {
    chatId: number;
    notificationMode?: TelegramChatMetadataDto.NotificationMode;
    enabled?: boolean;
}
export type TelegramChatDto = {
    id: number;
    type: TelegramChatDto.Type;
    title: string | null;
    username: string | null;
    firstName: string | null;
    lastName: string | null;
    smallPhotoUrl: string | null;
    bigPhotoUrl: string | null;
    bio: string | null;
    description: string | null;
    inviteLink: string | null;
    linkedChatId: string | null;
};

export namespace TelegramChatDto {
    export type Type = "PRIVATE" | "GROUP" | "SUPERGROUP" | "CHANNEL";
}

export type TelegramChatFetchResultDto = {
    found: boolean;
    chat: TelegramChatDto | null;
}
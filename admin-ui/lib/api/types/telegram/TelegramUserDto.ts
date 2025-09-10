export type TelegramUserDto = {
    id: number;
    isBot: boolean;
    firstName: string;
    lastName: string | null;
    username: string | null;
    languageCode: string | null;
    isPremium: boolean;
    addedToAttachmentMenu: boolean;
    canJoinGroups: boolean;
    canReadAllGroupMessages: boolean;
    supportsInlineQueries: boolean;
    canConnectToBusiness: boolean;
    hasMainWebApp: boolean;
};
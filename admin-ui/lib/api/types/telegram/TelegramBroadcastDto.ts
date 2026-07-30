export type TelegramBroadcastDto = {
    id: string;
    status: TelegramBroadcastDto.Status;
    silent: boolean;
    total: number;
    sent: number;
    failed: number;
    failures: TelegramBroadcastDto.Failure[];
    startedAt: string;
    finishedAt: string | null;
    error: string | null;
};

export namespace TelegramBroadcastDto {
    export type Status = "RUNNING" | "COMPLETED" | "FAILED";

    export type Failure = {
        chatId: number;
        chatTitle: string | null;
        error: string;
    };
}

export type TelegramBroadcastStatusDto = {
    recipients: number;
    latest: TelegramBroadcastDto | null;
};

export type TelegramBroadcastRequest = {
    text: string;
    silent: boolean;
};

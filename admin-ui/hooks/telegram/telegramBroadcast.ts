import {UseMutationResult, UseQueryResult} from "@tanstack/react-query";
import {useFetch, usePostMutate} from "@/hooks/apiHooks";
import {
    TelegramBroadcastDto,
    TelegramBroadcastRequest,
    TelegramBroadcastStatusDto
} from "@/lib/api/types/telegram/TelegramBroadcastDto";

const RUNNING_POLL_INTERVAL_MS = 1000;

/**
 * Recipient count plus the running (or most recent) broadcast. Polls while a broadcast is in progress, so
 * reloading the page mid-broadcast reattaches to it instead of losing the progress.
 */
export function useTelegramBroadcastStatus(): UseQueryResult<TelegramBroadcastStatusDto, Error> {
    return useFetch<TelegramBroadcastStatusDto>(
        ['telegram', 'broadcast', 'status'],
        '/telegram/broadcast/status',
        undefined,
        {
            refetchInterval: query =>
                query.state.data?.latest?.status === 'RUNNING' ? RUNNING_POLL_INTERVAL_MS : false,
        }
    );
}

export function useStartTelegramBroadcast(): UseMutationResult<
    TelegramBroadcastDto,
    Error,
    TelegramBroadcastRequest
> {
    return usePostMutate(['telegram', 'broadcast'], '/telegram/broadcast');
}

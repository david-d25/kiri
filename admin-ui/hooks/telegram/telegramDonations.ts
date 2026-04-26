import {UseMutationResult, useMutation, UseQueryResult, useQueryClient} from "@tanstack/react-query";
import {useFetch} from "@/hooks/apiHooks";
import {useCallback, useState} from "react";
import {api} from "@/lib/api/api";
import {PageableDto} from "@/lib/api/types/spring/pagination";
import {TelegramDonationDto, TelegramDonationsSummaryDto} from "@/lib/api/types/telegram/TelegramDonationDto";

export interface TelegramDonationsListParams {
    chatId?: number;
    page?: number;
    size?: number;
}

export interface TelegramDonationsListFunction {
    list: (params: TelegramDonationsListParams) => void;
}

export function useListTelegramDonations(): UseQueryResult<
    PageableDto<TelegramDonationDto[]>,
    Error
> & TelegramDonationsListFunction {
    const [params, setParams] = useState<TelegramDonationsListParams | null>(null);

    const buildUrl = (p: TelegramDonationsListParams) => {
        const query = new URLSearchParams();
        if (p.chatId !== undefined) query.set('chatId', p.chatId.toString());
        if (p.page !== undefined) query.set('page', p.page.toString());
        if (p.size !== undefined) query.set('size', p.size.toString());
        return `/telegram/donations?${query.toString()}`;
    };

    const result = useFetch<PageableDto<TelegramDonationDto[]>>(
        ['telegram', 'donations-list', params],
        params ? buildUrl(params) : '',
        undefined,
        {
            enabled: !!params,
            staleTime: 2000,
            retry: false,
        }
    );

    const list = useCallback((p: TelegramDonationsListParams) => {
        setParams(p);
    }, []);

    return { ...result, list };
}

export interface TelegramDonationsSummaryParams {
    chatId?: number;
}

export interface TelegramDonationsSummaryFunction {
    fetchSummary: (params: TelegramDonationsSummaryParams) => void;
}

export function useTelegramDonationsSummary(): UseQueryResult<
    TelegramDonationsSummaryDto,
    Error
> & TelegramDonationsSummaryFunction {
    const [params, setParams] = useState<TelegramDonationsSummaryParams | null>(null);

    const buildUrl = (p: TelegramDonationsSummaryParams) => {
        const query = new URLSearchParams();
        if (p.chatId !== undefined) query.set('chatId', p.chatId.toString());
        return `/telegram/donations/summary?${query.toString()}`;
    };

    const result = useFetch<TelegramDonationsSummaryDto>(
        ['telegram', 'donations-summary', params],
        params ? buildUrl(params) : '',
        undefined,
        {
            enabled: !!params,
            staleTime: 2000,
            retry: false,
        }
    );

    const fetchSummary = useCallback((p: TelegramDonationsSummaryParams) => {
        setParams(p);
    }, []);

    return { ...result, fetchSummary };
}

export function useRefundTelegramDonation(): UseMutationResult<
    TelegramDonationDto,
    Error,
    { telegramPaymentChargeId: string }
> {
    const qc = useQueryClient();
    return useMutation<TelegramDonationDto, Error, { telegramPaymentChargeId: string }>({
        mutationFn: vars => api.post<TelegramDonationDto, { telegramPaymentChargeId: string }>(
            `/telegram/donations/${encodeURIComponent(vars.telegramPaymentChargeId)}/refund`,
            vars
        ),
        onSuccess: (_data, vars) => {
            // Telegram delivers the refunded_payment service message asynchronously after refundStarPayment
            // returns, so a refetch right now would still see refunded=false. Patch the cache optimistically
            // and re-invalidate after a short delay to pick up the real refundedAt from the server.
            const nowSeconds = Math.floor(Date.now() / 1000);
            qc.setQueriesData<PageableDto<TelegramDonationDto[]>>(
                {queryKey: ['telegram', 'donations-list']},
                old => old ? {
                    ...old,
                    content: old.content.map(d =>
                        d.telegramPaymentChargeId === vars.telegramPaymentChargeId
                            ? {...d, refunded: true, refundedAt: d.refundedAt ?? nowSeconds}
                            : d
                    ),
                } : old
            );
            setTimeout(() => {
                qc.invalidateQueries({queryKey: ['telegram', 'donations-list']});
                qc.invalidateQueries({queryKey: ['telegram', 'donations-summary']});
            }, 3000);
        }
    });
}

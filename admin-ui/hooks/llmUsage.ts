import {UseQueryResult} from "@tanstack/react-query";
import {useFetch} from "@/hooks/apiHooks";
import {LlmUsageStatDto} from "@/lib/api/types/llmusage/LlmUsageStatDto";
import {LlmUsageDailyAggregateDto} from "@/lib/api/types/llmusage/LlmUsageDailyAggregateDto";
import {LlmUsageSummaryDto} from "@/lib/api/types/llmusage/LlmUsageSummaryDto";

export function useLlmUsageRecent(limit: number = 50): UseQueryResult<LlmUsageStatDto[], Error> {
    return useFetch<LlmUsageStatDto[]>(
        ['llm-usage', 'recent', limit],
        `/llm-usage/recent?limit=${limit}`,
        undefined,
        { staleTime: 10_000 }
    );
}

export type LlmUsageAggregateBucket = 'day' | 'hour';

export function useLlmUsageAggregate(
    from?: string,
    to?: string,
    bucket: LlmUsageAggregateBucket = 'day',
): UseQueryResult<LlmUsageDailyAggregateDto[], Error> {
    const params = new URLSearchParams();
    if (from) params.set('from', from);
    if (to) params.set('to', to);
    if (bucket !== 'day') params.set('bucket', bucket);
    const qs = params.toString();
    return useFetch<LlmUsageDailyAggregateDto[]>(
        ['llm-usage', 'aggregate', from, to, bucket],
        qs ? `/llm-usage/aggregate?${qs}` : '/llm-usage/aggregate',
        undefined,
        { staleTime: 10_000 }
    );
}

export function useLlmUsageSummary(): UseQueryResult<LlmUsageSummaryDto, Error> {
    return useFetch<LlmUsageSummaryDto>(
        ['llm-usage', 'summary'],
        '/llm-usage/summary',
        undefined,
        { staleTime: 10_000 }
    );
}

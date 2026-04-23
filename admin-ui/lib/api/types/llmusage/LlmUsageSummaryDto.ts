export interface LlmUsageSummaryWindowDto {
    name: string;
    fromTs: string;
    toTs: string;
    inputTokens: number;
    outputTokens: number;
    cacheReadInputTokens: number;
    cacheCreationInputTokens: number;
    requestCount: number;
    rawInputTokens: number;
    cacheHitRate: number;
}

export interface LlmUsageSummaryModelBreakdownDto {
    model: string;
    provider: string;
    inputTokens: number;
    outputTokens: number;
    cacheReadInputTokens: number;
    cacheCreationInputTokens: number;
    requestCount: number;
}

export interface LlmUsageSummaryDto {
    windows: LlmUsageSummaryWindowDto[];
    byModel: LlmUsageSummaryModelBreakdownDto[];
}

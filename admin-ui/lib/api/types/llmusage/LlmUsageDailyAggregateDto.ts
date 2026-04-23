export interface LlmUsageDailyAggregateDto {
    day: number;
    model: string;
    provider: string;
    inputTokens: number;
    outputTokens: number;
    cacheReadInputTokens: number;
    cacheCreationInputTokens: number;
    requestCount: number;
}

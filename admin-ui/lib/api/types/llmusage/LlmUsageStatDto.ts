export interface LlmUsageStatDto {
    id: string;
    timestamp: number;
    provider: string;
    model: string;
    inputTokens: number;
    outputTokens: number;
    cacheReadInputTokens: number;
    cacheCreationInputTokens: number;
    durationMs: number;
}

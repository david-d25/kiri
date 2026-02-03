export type ChatCompletionModelDto = {
    handle: string;
    features: ChatCompletionModelDto.Features;
    reasoningType: ChatCompletionModelDto.ReasoningType;
}

export namespace ChatCompletionModelDto {
    export type Features = {
        reasoning: boolean;
        webSearch: boolean;
        reasoningEffort: boolean;
        reasoningMaxTokens: boolean;
    }

    export type ReasoningType = "NONE" | "OPTIONAL" | "REQUIRED";
}
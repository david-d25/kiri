import {UseQueryResult} from "@tanstack/react-query";
import {ChatCompletionModelDto} from "@/lib/api/types/ChatCompletionModelDto";
import {useFetch} from "@/hooks/apiHooks";

export function useGetChatCompletionModels(): UseQueryResult<ChatCompletionModelDto[], Error> {
    return useFetch<ChatCompletionModelDto[]>(['chat-completion-models'], '/chat-completion/models');
}
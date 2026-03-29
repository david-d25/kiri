import {useFetch, usePostMutate} from "@/hooks/apiHooks";
import {ToolDto, ToolExecuteRequest} from "@/lib/api/types/ToolDto";
import {ToolCallFrameDto} from "@/lib/api/types/FrameDto";

export function useTools() {
    return useFetch<ToolDto[]>('tools', '/agent/tools');
}

export function useExecuteTool() {
    return usePostMutate<ToolCallFrameDto, ToolExecuteRequest>(['tools'], '/agent/tools/execute');
}

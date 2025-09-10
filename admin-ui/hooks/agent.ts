import {usePostMutate} from "@/hooks/apiHooks";
import {UseMutationResult} from "@tanstack/react-query";

export function useStartAgent(): UseMutationResult<undefined, Error, undefined> {
    return usePostMutate([], '/agent/start');
}

export function useStopAgent(): UseMutationResult<undefined, Error, undefined> {
    return usePostMutate([], '/agent/stop');
}

export function useTickAgent(): UseMutationResult<undefined, Error, undefined> {
    return usePostMutate([], '/agent/tick');
}

export function useHardStopAgent(): UseMutationResult<undefined, Error, undefined> {
    return usePostMutate([], '/agent/hard-stop');
}
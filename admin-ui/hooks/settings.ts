import {UseMutationResult, UseQueryResult} from "@tanstack/react-query";
import {useFetch, usePostMutate} from "@/hooks/apiHooks";
import {SettingDto} from "@/lib/api/types/SettingDto";
import {SettingUpdateRequestDto} from "@/lib/api/types/SettingUpdateRequestDto";

export function useGetSetting(key: string): UseQueryResult<SettingDto, Error> {
    return useFetch<SettingDto>(['settings', key], `/settings/${key}`);
}

export function useUpdateSetting(): UseMutationResult<SettingDto, Error, SettingUpdateRequestDto> {
    return usePostMutate(['settings'], dto => `/settings/${dto.key}`);
}
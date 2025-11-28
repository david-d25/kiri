import {UseMutationResult, UseQueryResult} from "@tanstack/react-query";
import {useFetch, usePostMutate} from "@/hooks/apiHooks";
import {SettingDto} from "@/lib/api/types/SettingDto";
import {SettingUpdateRequest} from "@/lib/api/types/SettingUpdateRequest";
import {SettingsUpdateRequest} from "@/lib/api/types/SettingsUpdateRequest";

export function useGetSettingsByKeys(keys: string[]): UseQueryResult<SettingDto[], Error> {
    const queryKey = ['settings'];
    const queryUrl = `/settings?keys=${keys.map(encodeURIComponent).join(',')}`;
    return useFetch<SettingDto[]>(queryKey, queryUrl);
}

export function useGetSetting(key: string): UseQueryResult<SettingDto, Error> {
    return useFetch<SettingDto>(['settings', key], `/settings/${key}`);
}

export function useUpdateSetting(key: string): UseMutationResult<SettingDto, Error, SettingUpdateRequest> {
    return usePostMutate(['settings'], `/settings/${key}`);
}

export function useUpdateSettings(): UseMutationResult<undefined, Error, SettingsUpdateRequest> {
    return usePostMutate(['settings'], () => '/settings');
}
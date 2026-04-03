import {useCallback, useState} from "react";
import {useMutation, UseQueryResult} from "@tanstack/react-query";
import {useFetch, usePostMutate, usePutMutate, useDeleteMutate} from "@/hooks/apiHooks";
import {api} from "@/lib/api/api";
import {MemoryStatsDto} from "@/lib/api/types/memory/MemoryStatsDto";
import {MemoryPointDto} from "@/lib/api/types/memory/MemoryPointDto";
import {MemoryPointDetailDto} from "@/lib/api/types/memory/MemoryPointDetailDto";
import {MemoryLinkDto} from "@/lib/api/types/memory/MemoryLinkDto";
import {MemorySearchResultDto} from "@/lib/api/types/memory/MemorySearchResultDto";
import {PageableDto} from "@/lib/api/types/spring/pagination";

export function useMemoryStats(): UseQueryResult<MemoryStatsDto, Error> {
    return useFetch<MemoryStatsDto>(
        ['memory', 'stats'],
        '/memory/stats',
        undefined,
        { staleTime: 5000 }
    );
}

export interface MemoryPointSearchParams {
    search?: string;
    page?: number;
    size?: number;
}

export interface MemoryPointSearchFunction {
    search: (params: MemoryPointSearchParams) => void;
}

export function useSearchMemoryPoints(): UseQueryResult<PageableDto<MemoryPointDto[]>, Error> & MemoryPointSearchFunction {
    const [params, setParams] = useState<MemoryPointSearchParams | null>(null);

    const buildUrl = (p: MemoryPointSearchParams) => {
        const query = new URLSearchParams();
        if (p.search) query.set('search', p.search);
        if (p.page !== undefined) query.set('page', p.page.toString());
        if (p.size !== undefined) query.set('size', p.size.toString());
        return `/memory/points?${query.toString()}`;
    };

    const result = useFetch<PageableDto<MemoryPointDto[]>>(
        ['memory', 'points', params],
        params ? buildUrl(params) : '',
        undefined,
        { enabled: !!params, staleTime: 2000, retry: false }
    );

    const search = useCallback((searchParams: MemoryPointSearchParams) => {
        setParams(searchParams);
    }, []);

    return { ...result, search };
}

export function useGetMemoryPointDetail(id: string | null): UseQueryResult<MemoryPointDetailDto, Error> {
    return useFetch<MemoryPointDetailDto>(
        ['memory', 'point-detail', id],
        id ? `/memory/points/${id}` : '',
        undefined,
        { enabled: !!id, staleTime: 2000 }
    );
}

export function useSemanticSearch() {
    return useMutation<MemorySearchResultDto[], Error, { query: string; limit?: number }>({
        mutationFn: (vars) => api.post<MemorySearchResultDto[]>('/memory/search', {
            query: vars.query,
            limit: vars.limit ?? 20
        }),
    });
}

export function useCreateMemoryPoint() {
    return usePostMutate<MemoryPointDetailDto, { value: string; keys: string[] }>(
        ['memory'],
        '/memory/points'
    );
}

export function useUpdateMemoryPoint() {
    return usePutMutate<MemoryPointDto, { id: string; value: string }>(
        ['memory'],
        (v) => `/memory/points/${v.id}`
    );
}

export function useDeleteMemoryPoint() {
    return useDeleteMutate<void, { id: string }>(
        ['memory'],
        (v) => `/memory/points/${v.id}`
    );
}

export function useDeleteMemoryKey() {
    return useDeleteMutate<void, { id: string }>(
        ['memory'],
        (v) => `/memory/keys/${v.id}`
    );
}

export function useUpdateMemoryLink() {
    return usePutMutate<MemoryLinkDto, { keyId: string; pointId: string; weight: number }>(
        ['memory'],
        (v) => `/memory/links/${v.keyId}/${v.pointId}`
    );
}

export function useDeleteMemoryLink() {
    return useDeleteMutate<void, { keyId: string; pointId: string }>(
        ['memory'],
        (v) => `/memory/links/${v.keyId}/${v.pointId}`
    );
}

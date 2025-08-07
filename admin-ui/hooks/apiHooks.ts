import {useQuery, useMutation, useQueryClient} from '@tanstack/react-query'
import { api } from '@/lib/api/api'
import {AxiosRequestConfig} from "axios";
import type {UndefinedInitialDataOptions} from "@tanstack/react-query/src/queryOptions";

function createQueryKey(key: string | any[]) {
    return Array.isArray(key) ? key : [key];
}

export function useFetch<T>(
    key: string | any[],
    url: string,
    axiosConfig?: AxiosRequestConfig,
    queryConfig?: Partial<UndefinedInitialDataOptions<T>>
) {
    return useQuery<T>({
        queryKey: createQueryKey(key),
        queryFn: () => api.get<T>(url, axiosConfig),
        refetchOnWindowFocus: false,
        retry(failureCount, error: any): boolean {
            if (error?.status === 403)
                return false;
            if (error?.status === 404)
                return false;
            return failureCount < 3;
        },
        ...queryConfig
    });
}

type MutationMethod = 'post' | 'put' | 'delete';

export function usePostMutate<TData, TVariables = any>(key: string | any[], url: string | ((v: TVariables) => string)) {
    return useMutate<TData, TVariables>(key, url, 'post');
}

export function usePutMutate<TData, TVariables = any>(key: string | any[], url: string | ((v: TVariables) => string)) {
    return useMutate<TData, TVariables>(key, url, 'put');
}

export function useDeleteMutate<TData>(key: string | any[], url: string | (() => string)) {
    return useMutate<TData, undefined>(key, url, 'delete');
}

function useMutate<TData, TVariables = any>(
    key: string | any[],
    url: string | ((v: TVariables) => string),
    method: MutationMethod = 'post'
) {
    const qc = useQueryClient();
    return useMutation<TData, Error, TVariables>({
        mutationFn: (vars: TVariables) => {
            if (typeof url === 'function') {
                url = url(vars);
            }
            switch (method) {
                case 'post':
                    return api.post<TData, TVariables>(url, vars)
                case 'put':
                    return api.put<TData, TVariables>(url, vars)
                case 'delete':
                    return api.delete<TData>(url)
            }
        },
        onSuccess: () => qc.invalidateQueries({ queryKey: createQueryKey(key) })
    });
}
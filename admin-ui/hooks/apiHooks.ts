import {useQuery, useMutation, useQueryClient} from '@tanstack/react-query'
import { api } from '../lib/api/api'
import {AxiosRequestConfig} from "axios";

function createQueryKey(key: string | any[]) {
    return Array.isArray(key) ? key : [key];
}

export function useFetch<T>(key: string | any[], url: string, config?: AxiosRequestConfig) {
    return useQuery<T>({
        queryKey: createQueryKey(key),
        queryFn: () => api.get<T>(url, config),
        refetchOnWindowFocus: false,
    });
}

type MutationMethod = 'post' | 'put' | 'delete';

export function usePostMutate<TData, TVariables = any>(key: string | any[], url: string) {
    return useMutate<TData, TVariables>(key, url, 'post');
}

export function usePutMutate<TData, TVariables = any>(key: string | any[], url: string) {
    return useMutate<TData, TVariables>(key, url, 'put');
}

export function useDeleteMutate<TData>(key: string | any[], url: string) {
    return useMutate<TData, undefined>(key, url, 'delete');
}

function useMutate<TData, TVariables = any>(
    key: string | any[],
    url: string,
    method: MutationMethod = 'post'
) {
    const qc = useQueryClient();
    return useMutation<TData, Error, TVariables>({
        mutationFn: (vars: TVariables) => {
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
import {QueryKey, useQueryClient} from "@tanstack/react-query";
import {useEffect} from "react";
import {api} from "../lib/api/api";

export function useSse<T>(
    key: QueryKey,
    endpoint: string,
    onMessage: (data: T) => void,
    onError?: (err: any) => void,
) {
    const qc = useQueryClient()
    useEffect(() => {
        return api.subscribe<T>(
            endpoint,
            data => {
                onMessage(data)
                qc.setQueryData<T[]>(key, old => (old ? [data, ...old] : [data]))
            },
            onError,
        )
    }, [endpoint, onMessage, onError, qc, key])
}
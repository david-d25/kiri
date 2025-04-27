import axios, {AxiosRequestConfig} from "axios";

const apiBase = process.env.NEXT_PUBLIC__API_BASE_URL!

const instance = axios.create({
    baseURL: apiBase,
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true
});

export const api = {
    get: <T>(url: string, config?: AxiosRequestConfig) =>
        instance.get<T>(url, config).then(res => res.data),

    post: <T, U = any>(url: string, data?: U, config?: AxiosRequestConfig) =>
        instance.post<T>(url, data, config).then(res => res.data),

    put: <T, U = any>(url: string, data?: U, config?: AxiosRequestConfig) =>
        instance.put<T>(url, data, config).then(res => res.data),

    delete: <T>(url: string, config?: AxiosRequestConfig) =>
        instance.delete<T>(url, config).then(res => res.data),

    subscribe: <T>(
        endpoint: string,
        onMessage: (data: T) => void,
        onError?: (err: any) => void,
    ) => {
        const es = new EventSource(`${instance.defaults.baseURL}${endpoint}`)
        es.onmessage = e => onMessage(JSON.parse(e.data))
        if (onError) es.onerror = onError
        return () => es.close()
    },
}
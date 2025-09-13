import axios, {AxiosRequestConfig} from "axios";
import Router from "next/router";

export const apiBase = process.env.NODE_ENV === 'production' ? '/kiri/api' : 'http://localhost:8080/api'

const instance = axios.create({
    baseURL: apiBase,
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true
});

// Redirecting to `/login` if there's authorization problem
instance.interceptors.response.use(
    response => {
        return response;
    },
    error => {
        if (error.response && error.response.status === 401 && typeof window !== 'undefined') {
            if (!window.location.pathname.startsWith('/login')) {
                const returnUrl = window.location.pathname + window.location.search;
                Router.push(`/login?returnUrl=${encodeURIComponent(returnUrl)}`).then(() => {});
            }
        }
        return Promise.reject(error);
    }
);

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
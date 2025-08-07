import {UseQueryResult} from "@tanstack/react-query";
import {useFetch} from "@/hooks/apiHooks";
import {useCallback, useState} from "react";
import {TelegramChatDto, TelegramChatFetchResultDto} from "@/lib/api/types/telegram/TelegramChatDto";
import {PageableDto} from "@/lib/api/types/spring/pagination";

export interface TelegramChatSearchParams {
    query?: string;
    titleContains?: string;
    usernameContains?: string;
    typeIn?: TelegramChatDto.Type[];
    page?: number;
    size?: number;
}

export interface TelegramChatSearchFunction {
    search: (params: TelegramChatSearchParams) => void;
}

export function useSearchTelegramChats(): UseQueryResult<
    PageableDto<TelegramChatDto[]>,
    Error
> & TelegramChatSearchFunction {
    const [params, setParams] = useState<TelegramChatSearchParams | null>(null);

    const buildUrl = (searchParams: TelegramChatSearchParams) => {
        const query = new URLSearchParams();
        if (searchParams.query) query.set('query', searchParams.query);
        if (searchParams.titleContains) query.set('titleContains', searchParams.titleContains);
        if (searchParams.usernameContains) query.set('usernameContains', searchParams.usernameContains);
        if (searchParams.typeIn) searchParams.typeIn.forEach(type => query.append('typeIn', type));
        if (searchParams.page) query.set('page', searchParams.page.toString());
        if (searchParams.size) query.set('size', searchParams.size.toString());
        return `/telegram/chats/search?${query.toString()}`;
    };

    const result = useFetch<PageableDto<TelegramChatDto[]>>(
        ['telegram-chats-search', params],
        params ? buildUrl(params) : '',
        undefined,
        {
            enabled: !!params,
            staleTime: 2000,
            retry: false,
        }
    );

    const search = useCallback((searchParams: TelegramChatSearchParams) => {
        setParams(searchParams);
    }, []);

    return { ...result, search };
}

interface TelegramChatFetchParams {
    chatId?: number;
    username?: string;
}

export interface TelegramChatFetchFunction {
    fetchById: (id: number) => void;
    fetchByUsername: (username: string) => void;
    reset: () => void;
}

export function useFetchTelegramChat(): UseQueryResult<TelegramChatFetchResultDto, Error> & TelegramChatFetchFunction {
    const [params, setParams] = useState<TelegramChatFetchParams | null>(null);
    const buildUrl = (params: TelegramChatFetchParams) => {
        const query = new URLSearchParams();
        if (params.chatId) query.set('chatId', params.chatId.toString());
        if (params.username) query.set('username', params.username);
        return `/telegram/chats/fetch?${query.toString()}`;
    };
    const result = useFetch<TelegramChatFetchResultDto>(
        ['telegram', 'chats', params],
        params ? buildUrl(params) : '',
        undefined,
        {
            enabled: !!params,
            staleTime: 2000,
            retry: false,
        }
    )
    const fetchById = useCallback((id: number) => {
        setParams({ chatId: id });
    }, []);
    const fetchByUsername = useCallback((username: string) => {
        setParams({ username });
    }, []);
    const reset = useCallback(() => {
        setParams(null);
    }, []);
    return { ...result, fetchById, fetchByUsername, reset };
}
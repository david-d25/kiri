import {useCallback, useState} from "react";
import {UseQueryResult} from "@tanstack/react-query";
import {useFetch, usePostMutate, usePutMutate, useDeleteMutate} from "@/hooks/apiHooks";
import {PageableDto} from "@/lib/api/types/spring/pagination";
import {CalendarEventDto} from "@/lib/api/types/calendar/CalendarEventDto";
import {CalendarEventCreateRequest} from "@/lib/api/types/calendar/CalendarEventCreateRequest";
import {CalendarEventUpdateRequest} from "@/lib/api/types/calendar/CalendarEventUpdateRequest";

export interface CalendarEventSearchParams {
    search?: string;
    page?: number;
    size?: number;
}

export interface CalendarEventSearchFunction {
    search: (params: CalendarEventSearchParams) => void;
}

export function useSearchCalendarEvents(): UseQueryResult<PageableDto<CalendarEventDto[]>, Error> & CalendarEventSearchFunction {
    const [params, setParams] = useState<CalendarEventSearchParams | null>(null);

    const buildUrl = (p: CalendarEventSearchParams) => {
        const q = new URLSearchParams();
        if (p.search) q.set('search', p.search);
        if (p.page !== undefined) q.set('page', p.page.toString());
        if (p.size !== undefined) q.set('size', p.size.toString());
        return `/calendar/events?${q.toString()}`;
    };

    const result = useFetch<PageableDto<CalendarEventDto[]>>(
        ['calendar', 'events', params],
        params ? buildUrl(params) : '',
        undefined,
        {enabled: !!params, staleTime: 2000, retry: false}
    );

    const search = useCallback((p: CalendarEventSearchParams) => setParams(p), []);
    return {...result, search};
}

export function useCreateCalendarEvent() {
    return usePostMutate<CalendarEventDto, CalendarEventCreateRequest>(
        ['calendar'],
        '/calendar/events'
    );
}

export function useUpdateCalendarEvent() {
    return usePutMutate<CalendarEventDto, { id: string } & CalendarEventUpdateRequest>(
        ['calendar'],
        (v) => `/calendar/events/${v.id}`
    );
}

export function useDeleteCalendarEvent() {
    return useDeleteMutate<void, { id: string }>(
        ['calendar'],
        (v) => `/calendar/events/${v.id}`
    );
}

export function useValidateRrule() {
    return usePostMutate<{ valid: boolean; error: string | null }, { rrule: string }>(
        ['calendar', 'validate-rrule'],
        '/calendar/validate-rrule'
    );
}

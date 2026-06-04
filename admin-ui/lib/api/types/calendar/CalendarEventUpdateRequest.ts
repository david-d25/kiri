export interface CalendarEventUpdateRequest {
    title: string;
    description?: string | null;
    timezone: string;
    firesAt?: string | null;
    rrule?: string | null;
    dtstart?: string | null;
    exdates?: string[];
    wakeAgent: boolean;
    missedPolicy: string;
    enabled: boolean;
}

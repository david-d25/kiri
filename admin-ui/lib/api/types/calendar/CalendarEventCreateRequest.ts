export interface CalendarEventCreateRequest {
    title: string;
    description?: string | null;
    timezone?: string | null;
    firesAt?: string | null;
    rrule?: string | null;
    dtstart?: string | null;
    wakeAgent?: boolean;
    missedPolicy?: string;
}

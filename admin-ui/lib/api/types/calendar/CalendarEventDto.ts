export interface CalendarEventDto {
    id: string;
    title: string;
    description: string | null;
    timezone: string;
    firesAt: string | null;
    rrule: string | null;
    dtstart: string | null;
    exdates: string[];
    nextFireAt: string | null;
    lastFiredAt: string | null;
    wakeAgent: boolean;
    missedPolicy: string;
    enabled: boolean;
    recurring: boolean;
    createdAt: string;
    updatedAt: string;
}

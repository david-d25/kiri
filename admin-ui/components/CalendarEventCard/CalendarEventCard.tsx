import {useEffect, useState} from "react";
import s from "./CalendarEventCard.module.scss";
import Button from "@/components/Button/Button";
import {CalendarEventDto} from "@/lib/api/types/calendar/CalendarEventDto";
import {useDeleteCalendarEvent} from "@/hooks/calendar";
import {toastService} from "@/services/ToastService";

type Props = {
    event: CalendarEventDto;
    onChanged?: () => void;
};

export default function CalendarEventCard({event, onChanged}: Props) {
    const [expanded, setExpanded] = useState(false);
    const deleteMutation = useDeleteCalendarEvent();

    useEffect(() => {
        if (deleteMutation.error) toastService.error(deleteMutation.error.message);
    }, [deleteMutation.error]);

    useEffect(() => {
        if (deleteMutation.isSuccess) {
            toastService.success("Event deleted");
            onChanged?.();
        }
    }, [deleteMutation.isSuccess]);

    function handleDelete(e: React.MouseEvent) {
        e.stopPropagation();
        if (confirm(`Delete event "${event.title}"?`)) {
            deleteMutation.mutate({id: event.id});
        }
    }

    const nextFireText = event.nextFireAt ? formatDateTime(event.nextFireAt) : (event.enabled ? "—" : "disabled");
    const shortHex = event.id.replace(/-/g, "").slice(0, 8);

    return (
        <div className={s.card}>
            <div className={s.header} onClick={() => setExpanded(!expanded)}>
                <div className={s.mainInfo}>
                    <div className={s.titleRow}>
                        <span className={s.title}>{event.title}</span>
                        <div className={s.badges}>
                            {event.recurring && <span className={`${s.badge} ${s.badgeRecurring}`}>recurring</span>}
                            {!event.enabled && <span className={`${s.badge} ${s.badgeDisabled}`}>disabled</span>}
                        </div>
                    </div>
                    <div className={s.subInfo}>
                        <span>next: {nextFireText}</span>
                        <span>tz: {event.timezone}</span>
                        <span>[{shortHex}]</span>
                    </div>
                </div>
                <div className={s.actions} onClick={e => e.stopPropagation()}>
                    <Button noStyle onClick={handleDelete}>Delete</Button>
                </div>
            </div>
            {expanded && (
                <div className={s.details}>
                    {event.recurring ? (
                        <>
                            <div>RRULE: <code>{event.rrule}</code></div>
                            {event.dtstart && <div>dtstart: {formatDateTime(event.dtstart)}</div>}
                            <div>missed policy: {event.missedPolicy}</div>
                            {event.exdates.length > 0 && (
                                <div>cancelled occurrences: {event.exdates.length}</div>
                            )}
                        </>
                    ) : (
                        event.firesAt && <div>fires at: {formatDateTime(event.firesAt)}</div>
                    )}
                    {event.lastFiredAt && <div>last fired: {formatDateTime(event.lastFiredAt)}</div>}
                    <div>wake agent: {event.wakeAgent ? "yes" : "no"}</div>
                    <div>created: {formatDateTime(event.createdAt)}</div>
                    {event.description && <div className={s.descriptionBlock}>{event.description}</div>}
                </div>
            )}
        </div>
    );
}

function formatDateTime(iso: string): string {
    try {
        const d = new Date(iso);
        if (isNaN(d.getTime())) return iso;
        return d.toLocaleString();
    } catch {
        return iso;
    }
}

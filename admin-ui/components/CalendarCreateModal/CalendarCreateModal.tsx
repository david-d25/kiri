import {useState} from "react";
import s from "./CalendarCreateModal.module.scss";
import TextArea from "@/components/TextArea/TextArea";
import TextInput from "@/components/TextInput/TextInput";
import Button from "@/components/Button/Button";
import Toggle from "@/components/Toggle/Toggle";
import {ModalComponentProps} from "@/components/Modal/ModalProvider";
import {CalendarEventCreateRequest} from "@/lib/api/types/calendar/CalendarEventCreateRequest";

export type CalendarCreateResult = CalendarEventCreateRequest;

type Props = ModalComponentProps<CalendarCreateResult>;

const RRULE_EXAMPLES = `FREQ=DAILY;BYHOUR=9;BYMINUTE=0           — every day at 09:00
FREQ=WEEKLY;BYDAY=MO,WE,FR;BYHOUR=14     — Mon/Wed/Fri at 14:00
FREQ=MONTHLY;BYMONTHDAY=1                — 1st of every month
FREQ=YEARLY;BYMONTH=12;BYMONTHDAY=31     — every Dec 31`;

const localTz = (() => {
    try { return Intl.DateTimeFormat().resolvedOptions().timeZone; } catch { return "UTC"; }
})();

function defaultFiresAtLocal(): string {
    const d = new Date();
    d.setMinutes(d.getMinutes() + 60 - d.getMinutes() % 60);
    d.setSeconds(0, 0);
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, "0");
    const dd = String(d.getDate()).padStart(2, "0");
    const hh = String(d.getHours()).padStart(2, "0");
    const mi = String(d.getMinutes()).padStart(2, "0");
    return `${yyyy}-${mm}-${dd}T${hh}:${mi}`;
}

export default function CalendarCreateModal({onSubmit, onCancel}: Props) {
    const [title, setTitle] = useState("");
    const [description, setDescription] = useState("");
    const [recurring, setRecurring] = useState(false);
    const [firesAtLocal, setFiresAtLocal] = useState(defaultFiresAtLocal());
    const [rrule, setRrule] = useState("FREQ=DAILY;BYHOUR=9;BYMINUTE=0");
    const [dtstartLocal, setDtstartLocal] = useState(defaultFiresAtLocal());
    const [wakeAgent, setWakeAgent] = useState(true);

    function localToIso(local: string): string | null {
        if (!local) return null;
        const d = new Date(local);
        if (isNaN(d.getTime())) return null;
        return d.toISOString();
    }

    function handleCreate() {
        if (!title.trim()) return;
        const payload: CalendarEventCreateRequest = {
            title: title.trim(),
            description: description.trim() || null,
            timezone: localTz,
            wakeAgent,
            missedPolicy: "FIRE_ONCE",
        };
        if (recurring) {
            payload.rrule = rrule.trim();
            payload.dtstart = localToIso(dtstartLocal);
        } else {
            payload.firesAt = localToIso(firesAtLocal);
        }
        onSubmit(payload);
    }

    const canSubmit = title.trim().length > 0 &&
        (recurring ? (rrule.trim().length > 0 && !!localToIso(dtstartLocal))
                   : !!localToIso(firesAtLocal));

    return (
        <div className={s.root}>
            <div className={s.field}>
                <TextInput value={title} onChange={setTitle} label="Title" placeholder="Reminder title..."/>
            </div>

            <div className={s.toggleRow}>
                <Toggle checked={recurring} onChange={setRecurring} label="Recurring"/>
                <Toggle checked={wakeAgent} onChange={setWakeAgent} label="Wake agent"/>
            </div>

            {!recurring && (
                <div className={s.field}>
                    <label className={s.hint}>Fires at (local time, {localTz})</label>
                    <input
                        type="datetime-local"
                        value={firesAtLocal}
                        onChange={e => setFiresAtLocal(e.target.value)}
                    />
                </div>
            )}

            {recurring && (
                <>
                    <div className={s.field}>
                        <label className={s.hint}>dtstart (local time, {localTz})</label>
                        <input
                            type="datetime-local"
                            value={dtstartLocal}
                            onChange={e => setDtstartLocal(e.target.value)}
                        />
                    </div>
                    <div className={s.field}>
                        <TextInput value={rrule} onChange={setRrule} label="RRULE" placeholder="FREQ=DAILY;..."/>
                        <pre className={s.rruleExamples}>{RRULE_EXAMPLES}</pre>
                    </div>
                </>
            )}

            <div className={s.field}>
                <TextArea
                    value={description}
                    onChange={setDescription}
                    label="Description (optional)"
                    rows={3}
                />
            </div>

            <div className={s.actions}>
                <Button onClick={onCancel}>Cancel</Button>
                <Button onClick={handleCreate} colorAccent="primary" disabled={!canSubmit}>
                    Create
                </Button>
            </div>
        </div>
    );
}

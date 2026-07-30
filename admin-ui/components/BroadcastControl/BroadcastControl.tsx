import s from "./BroadcastControl.module.scss";
import {JSX, useEffect, useState} from "react";
import {isAxiosError} from "axios";
import Container from "@/components/Container/Container";
import Panel from "@/components/Panel/Panel";
import TextArea from "@/components/TextArea/TextArea";
import Toggle from "@/components/Toggle/Toggle";
import Button from "@/components/Button/Button";
import InfoPanel from "@/components/InfoPanel/InfoPanel";
import Collapsible from "@/components/Collapsible/Collapsible";
import {modalService} from "@/services/ModalService";
import {toastService} from "@/services/ToastService";
import {useStartTelegramBroadcast, useTelegramBroadcastStatus} from "@/hooks/telegram/telegramBroadcast";
import {TelegramBroadcastDto} from "@/lib/api/types/telegram/TelegramBroadcastDto";
import BroadcastConfirmModal, {BroadcastConfirmResult} from "@/components/BroadcastConfirmModal/BroadcastConfirmModal";

const MAX_MESSAGE_LENGTH = 4096;

export default function BroadcastControl() {
    const statusReq = useTelegramBroadcastStatus();
    const startReq = useStartTelegramBroadcast();

    const [text, setText] = useState('');
    const [silent, setSilent] = useState(false);

    const latest = statusReq.data?.latest ?? null;
    const recipients = statusReq.data?.recipients ?? 0;
    const running = latest?.status === 'RUNNING';
    const busy = running || startReq.isPending;

    useEffect(() => {
        if (startReq.error) {
            toastService.error(errorMessage(startReq.error));
        }
    }, [startReq.error]);

    useEffect(() => {
        if (startReq.isSuccess) {
            toastService.success('Broadcast started');
            setText('');
        }
    }, [startReq.isSuccess]);

    return (
        <div className={s.root}>
            <Container>
                <Panel className={s.panel}>
                    <div className={s.header}>
                        <div className={s.title}>Broadcast</div>
                        <div className={s.subtitle}>
                            Sends one message to every enabled chat. Chats that blocked the bot are skipped and
                            reported below.
                        </div>
                    </div>
                    <TextArea
                        value={text}
                        onChange={setText}
                        label={'Message (Telegram HTML: <b>, <i>, <code>, <a href="…">)'}
                        placeholder={'Message to send to all enabled chats'}
                        rows={5}
                        maxLength={MAX_MESSAGE_LENGTH}
                        disabled={busy}
                        spellcheck={false}
                    />
                    <div className={s.controls}>
                        <Toggle
                            checked={silent}
                            onChange={setSilent}
                            disabled={busy}
                            label={'Silent'}
                        />
                        <div className={s.recipients}>
                            {statusReq.isLoading
                                ? 'Loading recipients…'
                                : `${recipients} enabled chat${recipients === 1 ? '' : 's'}`}
                        </div>
                        <Button
                            onClick={onSendClick}
                            colorAccent={'primary'}
                            disabled={busy || !text.trim() || recipients === 0}
                        >
                            {busy ? 'Sending…' : 'Send broadcast'}
                        </Button>
                    </div>
                    {statusReq.isError && (
                        <InfoPanel type={'error'}>Loading error: {statusReq.error.message}</InfoPanel>
                    )}
                    {latest && lastBroadcast(latest)}
                </Panel>
            </Container>
        </div>
    );

    async function onSendClick() {
        const message = text.trim();
        if (!message || busy || recipients === 0) {
            return;
        }
        const result = await modalService.open<BroadcastConfirmResult, {
            text: string,
            recipients: number,
            silent: boolean
        }>({
            title: 'Confirm broadcast',
            content: BroadcastConfirmModal,
            props: {text: message, recipients, silent},
            closeOnBackdropClick: true,
            closeOnEscape: true,
        });
        if (!result?.confirmed) {
            return;
        }
        startReq.mutate({text: message, silent});
    }
}

function lastBroadcast(broadcast: TelegramBroadcastDto): JSX.Element {
    const processed = broadcast.sent + broadcast.failed;
    const percent = broadcast.total > 0 ? Math.round(processed / broadcast.total * 100) : 0;

    return (
        <div className={s.progress}>
            <div className={s.progressHeader}>
                <span className={s.progressStatus} data-status={broadcast.status}>{statusLabel(broadcast)}</span>
                <span className={s.progressCounts}>
                    {broadcast.sent} sent
                    {broadcast.failed > 0 && ` · ${broadcast.failed} failed`}
                    {` · ${processed}/${broadcast.total}`}
                </span>
            </div>
            <div className={s.progressBar}>
                <div className={s.progressBarFill} data-status={broadcast.status} style={{width: `${percent}%`}}/>
            </div>
            {broadcast.error && <InfoPanel type={'error'}>Broadcast aborted: {broadcast.error}</InfoPanel>}
            {broadcast.failures.length > 0 && (
                <Collapsible header={<span className={s.failuresHeader}>
                    Failed deliveries ({broadcast.failed})
                </span>}>
                    <div className={s.failures}>
                        {broadcast.failures.map(failure => (
                            <div className={s.failure} key={failure.chatId}>
                                <div className={s.failureChat}>
                                    {failure.chatTitle ?? '(unknown)'}
                                    <span className={s.failureChatId}>{failure.chatId}</span>
                                </div>
                                <div className={s.failureError}>{failure.error}</div>
                            </div>
                        ))}
                        {broadcast.failed > broadcast.failures.length && (
                            <div className={s.failuresTruncated}>
                                …and {broadcast.failed - broadcast.failures.length} more, see server logs
                            </div>
                        )}
                    </div>
                </Collapsible>
            )}
        </div>
    );
}

/** Rejections carry the reason in the ErrorResponse body, not in the generic axios message. */
function errorMessage(error: Error): string {
    if (isAxiosError<{ message?: string }>(error) && error.response?.data?.message) {
        return error.response.data.message;
    }
    return error.message;
}

function statusLabel(broadcast: TelegramBroadcastDto): string {
    switch (broadcast.status) {
        case 'RUNNING':
            return 'Broadcasting…';
        case 'COMPLETED':
            return broadcast.failed > 0 ? 'Finished with failures' : 'Finished';
        case 'FAILED':
            return 'Failed';
    }
}

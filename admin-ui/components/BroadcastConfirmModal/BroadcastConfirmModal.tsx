import s from "./BroadcastConfirmModal.module.scss";
import Button from "@/components/Button/Button";
import {ModalComponentProps} from "@/components/Modal/ModalProvider";

export type BroadcastConfirmResult = { confirmed: true };

type Props = ModalComponentProps<BroadcastConfirmResult> & {
    text: string;
    recipients: number;
    silent: boolean;
};

export default function BroadcastConfirmModal({onSubmit, onCancel, text, recipients, silent}: Props) {
    const chats = `${recipients} enabled chat${recipients === 1 ? '' : 's'}`;

    return (
        <div className={s.root}>
            <p className={s.warning}>
                This will send the message to {chats}. Messages already delivered cannot be recalled.
            </p>
            <dl className={s.details}>
                <dt>Recipients</dt>
                <dd>{chats}</dd>

                <dt>Notification</dt>
                <dd>{silent ? 'Silent' : 'Normal'}</dd>

                <dt>Length</dt>
                <dd>{text.length} characters</dd>
            </dl>
            <div className={s.previewLabel}>Message source</div>
            <pre className={s.preview}>{text}</pre>
            <div className={s.actions}>
                <Button onClick={onCancel}>Cancel</Button>
                <Button onClick={() => onSubmit({confirmed: true})} colorAccent="primary">
                    Send
                </Button>
            </div>
        </div>
    );
}

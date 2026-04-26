import s from "./RefundConfirmModal.module.scss";
import Button from "@/components/Button/Button";
import {ModalComponentProps} from "@/components/Modal/ModalProvider";
import {TelegramDonationDto} from "@/lib/api/types/telegram/TelegramDonationDto";

const REFUND_WINDOW_DAYS = 21;

export type RefundConfirmResult = { confirmed: true };

type Props = ModalComponentProps<RefundConfirmResult> & {
    donation: TelegramDonationDto;
};

function formatStarsAmount(amount: number, currency: string): string {
    return currency === 'XTR' ? `${amount} ⭐` : `${amount} ${currency}`;
}

function formatRefundWindow(paidAt: number): string {
    const paid = paidAt * 1000;
    const deadline = paid + REFUND_WINDOW_DAYS * 24 * 60 * 60 * 1000;
    const remainingMs = deadline - Date.now();
    if (remainingMs <= 0) {
        return "Refund window expired";
    }
    const days = Math.floor(remainingMs / (24 * 60 * 60 * 1000));
    const hours = Math.floor((remainingMs % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000));
    if (days >= 1) {
        return `${days}d ${hours}h remaining`;
    }
    return `${hours}h remaining`;
}

export default function RefundConfirmModal({onSubmit, onCancel, donation}: Props) {
    const amount = formatStarsAmount(donation.totalAmount, donation.currency);
    const userDisplay = donation.userUsername
        ? `@${donation.userUsername}`
        : donation.userFirstName ?? (donation.userId != null ? `user ${donation.userId}` : '(unknown)');
    const userIdSuffix = donation.userId != null ? ` (id ${donation.userId})` : '';
    const chatDisplay = donation.chatUsername
        ? `@${donation.chatUsername}`
        : donation.chatTitle ?? `chat ${donation.chatId}`;
    const refundWindow = formatRefundWindow(donation.paidAt);
    const expired = refundWindow === "Refund window expired";

    return (
        <div className={s.root}>
            <p className={s.warning}>
                This will refund {amount} back to {userDisplay}. The action cannot be undone.
            </p>
            <dl className={s.details}>
                <dt>Amount</dt>
                <dd>{amount}</dd>

                <dt>User</dt>
                <dd>{userDisplay}{userIdSuffix}</dd>

                <dt>Chat</dt>
                <dd>{chatDisplay} (id {donation.chatId})</dd>

                <dt>Charge id</dt>
                <dd className={s.mono}>{donation.telegramPaymentChargeId}</dd>

                <dt>Paid</dt>
                <dd>{new Date(donation.paidAt * 1000).toLocaleString()}</dd>

                <dt>Refund window</dt>
                <dd className={expired ? s.expired : undefined}>{refundWindow}</dd>
            </dl>
            <div className={s.actions}>
                <Button onClick={onCancel}>Cancel</Button>
                <Button
                    onClick={() => onSubmit({confirmed: true})}
                    colorAccent="primary"
                    disabled={expired}
                >
                    Refund
                </Button>
            </div>
        </div>
    );
}

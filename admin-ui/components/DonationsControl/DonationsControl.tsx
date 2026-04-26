import s from "./DonationsControl.module.scss";
import {JSX, useEffect, useState} from "react";
import LoadingOverlay from "@/components/LoadingOverlay/LoadingOverlay";
import InfoPanel from "@/components/InfoPanel/InfoPanel";
import SkeletonLoader from "@/components/SkeletonLoader/SkeletonLoader";
import Container from "@/components/Container/Container";
import PageSelector from "@/components/PageSelector/PageSelector";
import Button from "@/components/Button/Button";
import TabNavigation from "@/components/TabNavigation/TabNavigation";
import DonationSettings from "@/components/DonationSettings/DonationSettings";
import {
    useListTelegramDonations,
    useRefundTelegramDonation,
    useTelegramDonationsSummary
} from "@/hooks/telegram/telegramDonations";
import {TelegramDonationDto, TelegramDonationsSummaryDto} from "@/lib/api/types/telegram/TelegramDonationDto";
import {toastService} from "@/services/ToastService";
import {modalService} from "@/services/ModalService";
import RefundConfirmModal, {RefundConfirmResult} from "@/components/RefundConfirmModal/RefundConfirmModal";

const PAGE_SIZE = 20;

type DonationsTab = 'list' | 'settings';

const TABS: {id: DonationsTab; title: string}[] = [
    {id: 'list', title: 'Donations'},
    {id: 'settings', title: 'Settings'},
];

function formatStarsAmount(amount: number, currency: string): string {
    return currency === 'XTR' ? `${amount}⭐` : `${amount} ${currency}`;
}

export default function DonationsControl() {
    const [tab, setTab] = useState<DonationsTab>('list');
    return (
        <div className={s.root}>
            <Container>
                <div className={s.tabBar}>
                    <TabNavigation<DonationsTab>
                        tabs={TABS}
                        tabId={tab}
                        onTabChange={setTab}
                    />
                </div>
                {tab === 'list' && <DonationsList/>}
                {tab === 'settings' && <DonationSettings/>}
            </Container>
        </div>
    );
}

function DonationsList() {
    const list = useListTelegramDonations();
    const summary = useTelegramDonationsSummary();
    const [pageIndex, setPageIndex] = useState(0);
    const {list: listFn} = list;
    const {fetchSummary} = summary;

    useEffect(() => {
        listFn({page: pageIndex, size: PAGE_SIZE});
    }, [pageIndex, listFn]);

    useEffect(() => {
        fetchSummary({});
    }, [fetchSummary]);

    return (
        <Container>
            <div className={s.body}>
                {summary.data && <SummaryPanel summary={summary.data}/>}
                {summary.isLoading && !summary.data && <SkeletonLoader height={80} borderRadius={5}/>}
                {list.isError && (
                    <InfoPanel type={'error'}>Loading error: {list.error.message}</InfoPanel>
                )}
                <LoadingOverlay loading={list.isFetching}>
                    <div className={s.donationsList}>
                        {list.isLoading && !list.data && <SkeletonLoader height={75} borderRadius={5} count={6}/>}
                        {list.data && donationList()}
                    </div>
                </LoadingOverlay>
                {!!list.data?.totalPages && (
                    <PageSelector
                        pages={list.data.totalPages}
                        currentPage={pageIndex}
                        onPageChange={setPageIndex}
                    />
                )}
            </div>
        </Container>
    );

    function donationList(): JSX.Element {
        if (list.data?.content?.length) {
            return (
                <>
                    {list.data.content.map(donation => (
                        <DonationRow
                            key={donation.telegramPaymentChargeId}
                            donation={donation}
                        />
                    ))}
                </>
            );
        }
        return <div className={s.empty}>No donations</div>;
    }
}

type SummaryPanelProps = { summary: TelegramDonationsSummaryDto };

function SummaryPanel({summary}: SummaryPanelProps) {
    return (
        <div className={s.summary}>
            <SummaryStat label="Received" value={formatStarsAmount(summary.totalReceived, summary.currency)}/>
            <SummaryStat label="Refunded" value={formatStarsAmount(summary.totalRefunded, summary.currency)}/>
            <SummaryStat label="Net" value={formatStarsAmount(summary.net, summary.currency)}/>
            <SummaryStat label="Donations" value={`${summary.countTotal}`}/>
            <SummaryStat label="Refunds" value={`${summary.countRefunded}`}/>
        </div>
    );
}

function SummaryStat({label, value}: { label: string, value: string }) {
    return (
        <div className={s.stat}>
            <div className={s.statLabel}>{label}</div>
            <div className={s.statValue}>{value}</div>
        </div>
    );
}

type DonationRowProps = {
    donation: TelegramDonationDto;
};

function DonationRow({donation}: DonationRowProps) {
    const refundReq = useRefundTelegramDonation();
    const formattedAmount = formatStarsAmount(donation.totalAmount, donation.currency);

    const userDisplay = donation.userUsername
        ? `@${donation.userUsername}`
        : donation.userFirstName ?? (donation.userId != null ? `user ${donation.userId}` : '(unknown)');

    const chatDisplay = donation.chatUsername
        ? `@${donation.chatUsername}`
        : donation.chatTitle ?? `chat ${donation.chatId}`;

    useEffect(() => {
        if (refundReq.error) {
            toastService.error(refundReq.error.message);
        }
    }, [refundReq.error]);

    useEffect(() => {
        if (refundReq.isSuccess) {
            toastService.success('Refund issued');
        }
    }, [refundReq.isSuccess]);

    async function onRefundClick() {
        if (donation.refunded) return;
        const result = await modalService.open<RefundConfirmResult, { donation: TelegramDonationDto }>({
            title: 'Confirm refund',
            content: RefundConfirmModal,
            props: {donation},
            closeOnBackdropClick: true,
            closeOnEscape: true,
        });
        if (!result?.confirmed) return;
        refundReq.mutate({telegramPaymentChargeId: donation.telegramPaymentChargeId});
    }

    return (
        <div className={s.donation} data-refunded={donation.refunded}>
            <div className={s.amount}>{formattedAmount}</div>
            <div className={s.parties}>
                <div className={s.user}>{userDisplay}</div>
                <div className={s.chat}>in {chatDisplay}</div>
            </div>
            <div className={s.meta}>
                <div className={s.date}>{new Date(donation.paidAt * 1000).toLocaleString()}</div>
                <div className={s.chargeId} title={donation.telegramPaymentChargeId}>
                    {donation.telegramPaymentChargeId}
                </div>
            </div>
            <div className={s.status}>
                {donation.refunded ? (
                    <span className={s.refundedBadge}>
                        Refunded
                        {donation.refundedAt && ` · ${new Date(donation.refundedAt * 1000).toLocaleDateString()}`}
                    </span>
                ) : (
                    <Button
                        onClick={onRefundClick}
                        disabled={refundReq.isPending}
                    >
                        {refundReq.isPending ? 'Refunding…' : 'Refund'}
                    </Button>
                )}
            </div>
        </div>
    );
}

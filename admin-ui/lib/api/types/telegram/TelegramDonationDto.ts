export type TelegramDonationDto = {
    telegramPaymentChargeId: string;
    providerPaymentChargeId: string;
    chatId: number;
    chatTitle: string | null;
    chatUsername: string | null;
    userId: number | null;
    userUsername: string | null;
    userFirstName: string | null;
    currency: string;
    totalAmount: number;
    invoicePayload: string;
    paidAt: number;
    refunded: boolean;
    refundedAt: number | null;
    messageId: number;
};

export type TelegramDonationsSummaryDto = {
    totalReceived: number;
    totalRefunded: number;
    net: number;
    countTotal: number;
    countRefunded: number;
    currency: string;
};

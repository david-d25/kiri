export interface BootstrapDto {
    isAuthenticated: boolean
    user?: {
        id: string
        isAdmin: boolean
        firstName: string
        lastName?: string
        username?: string
        photoUrl?: string
    }
    login?: {
        telegramBotUsername: string
        telegramAuthCallbackUrl: string
    }
}
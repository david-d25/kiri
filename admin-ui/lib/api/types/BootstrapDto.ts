import {UserDto} from "@/lib/api/types/UserDto";

export interface BootstrapDto {
    isAuthenticated: boolean
    user?: {
        id: number
        role: UserDto.Role
        firstName: string
        lastName?: string
        username?: string
        photoUrl?: string
    }
    login?: {
        telegramBotUsername: string
        telegramAuthCallbackUrl: string
    },
    version: string
}
import {TelegramUserDto} from "@/lib/api/types/telegram/TelegramUserDto";

export type UserDto = {
    id: number;
    telegramUser: TelegramUserDto;
    role: UserDto.Role;
    smallPhotoUrl: string | null;
    bigPhotoUrl: string | null;
};

export namespace UserDto {
    export type Role = "ADMIN";
}
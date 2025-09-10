import {UserDto} from "@/lib/api/types/UserDto";

export type UserCreateRequestDto = {
    telegramUserId: number;
    role: UserDto.Role
};
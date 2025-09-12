import {UserDto} from "@/lib/api/types/UserDto";

export type UserCreateRequest = {
    telegramUserId: number;
    role: UserDto.Role
};
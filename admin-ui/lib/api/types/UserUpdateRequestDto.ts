import {UserDto} from "@/lib/api/types/UserDto";

export type UserUpdateRequestDto = {
    id: number;
    role: UserDto.Role;
};
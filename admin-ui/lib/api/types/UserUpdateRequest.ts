import {UserDto} from "@/lib/api/types/UserDto";

export type UserUpdateRequest = {
    id: number;
    role: UserDto.Role;
};
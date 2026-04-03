import {MemoryLinkDto} from "./MemoryLinkDto";

export interface MemoryPointDetailDto {
    id: string;
    value: string;
    createdAt: string;
    links: MemoryLinkDto[];
}

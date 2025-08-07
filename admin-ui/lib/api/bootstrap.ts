import {BootstrapDto} from "./types/BootstrapDto";
import {api} from "./api";

export function fetchBootstrap(): Promise<BootstrapDto> {
    return api.get<BootstrapDto>('/bootstrap')
}
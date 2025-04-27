import {useFetch} from "./apiHooks";
import {BootstrapDto} from "../lib/types/BootstrapDto";

export function useBootstrap() {
    return useFetch<BootstrapDto>('bootstrap', '/bootstrap')
}
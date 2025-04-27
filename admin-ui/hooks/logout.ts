import {usePostMutate} from "./apiHooks";

export function useLogout() {
    return usePostMutate<void>('logout', '/logout')
}
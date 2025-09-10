import {useFetch} from "./apiHooks";
import {UseQueryResult} from "@tanstack/react-query";

export function useFetchDevAuthHash(
    userId: number,
    firstName: string,
    lastName: string,
    username: string,
    photoUrl: string,
    authDate: Date,
): UseQueryResult<string, Error> {
    const authDateTimestamp = Math.floor(authDate.getTime() / 1000);
    const params = new URLSearchParams({
        userId: userId.toString(),
        firstName: encodeURIComponent(firstName),
        lastName: encodeURIComponent(lastName),
        username: encodeURIComponent(username),
        photoUrl: encodeURIComponent(photoUrl),
        authDate: authDateTimestamp.toString()
    });

    return useFetch<string>(['devAuthHash', userId], `/auth/telegram/dev/hash?${params.toString()}`);
}

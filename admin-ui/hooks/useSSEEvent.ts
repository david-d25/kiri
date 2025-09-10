import {useEffect, useState} from "react";
import {EventMap, sseClient, SSEventName} from "@/lib/sseClient";

export function useSSEEvent<K extends SSEventName>(
    name: K,
    initial: EventMap[K]
): EventMap[K] {
    const [value, setValue] = useState<EventMap[K]>(initial);

    useEffect(() => {
        return sseClient.on<K>(name, (data) => setValue(data));
    }, [name]);

    return value;
}
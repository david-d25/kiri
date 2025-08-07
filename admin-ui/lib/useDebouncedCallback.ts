import {useCallback, useEffect, useRef} from "react";

export function useDebouncedCallback<T extends (...args: any[]) => void>(
    callback: T,
    delay = 300
) {
    const timer = useRef<ReturnType<typeof setTimeout>>(undefined);

    const debounced = useCallback(
        (...args: Parameters<T>) => {
            clearTimeout(timer.current);
            timer.current = setTimeout(() => callback(...args), delay);
        },
        [callback, delay]
    );

    useEffect(() => () => clearTimeout(timer.current), [delay]);

    return debounced;
}
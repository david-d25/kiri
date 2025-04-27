export function classnames(...args: Array<object | string | undefined | null>): string {
    return args
        .flatMap(arg => {
            if (!arg) return [];
            if (typeof arg === 'string') return [arg];
            if (Array.isArray(arg)) return arg.filter(Boolean);
            return Object.entries(arg as Record<string, unknown>)
                .filter(([, value]) => value)
                .map(([key]) => key);
        })
        .join(' ');
}
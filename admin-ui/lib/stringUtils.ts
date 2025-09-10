export function truncateWithEllipsis(text: string, limit: number): string {
    if (text.length < limit - 3) {
        return text
    }
    return text.slice(0, limit - 3) + '...';
}
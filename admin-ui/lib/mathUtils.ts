export function clamp(x: number, min: number | null, max: number | null): number {
    if (min !== null && x < min) {
        return min;
    }
    if (max !== null && x > max) {
        return max;
    }
    return x;
}
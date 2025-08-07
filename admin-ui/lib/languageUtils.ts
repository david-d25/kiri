export function russianDeclension(n: number, ...forms: string[]): string {
    const absN = Math.abs(n) % 100;
    if (absN >= 11 && absN <= 14) {
        return forms[2];
    }
    switch (absN % 10) {
        case 1:
            return forms[0];
        case 2:
        case 3:
        case 4:
            return forms[1];
        default:
            return forms[2];
    }
}
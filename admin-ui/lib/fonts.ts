import localFont from 'next/font/local';

export const jetBrainsMono = localFont({
    src: [
        { path: '../fonts/JetBrainsMono.woff2', weight: '100 900', style: 'normal' },
        { path: '../fonts/JetBrainsMono-Italic.woff2', weight: '100 900', style: 'italic' },
    ],
    variable: '--jetbrains-mono-font',
    display: 'swap',
});
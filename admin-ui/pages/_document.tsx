import Document, { Html, Head, Main, NextScript } from 'next/document';
import {jetBrainsMono} from "@/lib/fonts";

export default class AppDocument extends Document {
    render() {
        return (
            <Html>
                <Head/>
                <body className={jetBrainsMono.variable}>
                    <Main/>
                    <NextScript/>
                </body>
            </Html>
        );
    }
}
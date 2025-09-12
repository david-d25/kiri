import type { AppProps } from 'next/app'

import '../styles/globals.scss'
import Head from "next/head";
import getConfig from 'next/config'
import {HydrationBoundary, QueryClient, QueryClientProvider} from "@tanstack/react-query";
import {useState} from "react";

import {jetBrainsMono} from "@/lib/fonts";
import {ModalProvider} from "@/components/Modal/ModalProvider";
import {ModalServiceInitializer} from "@/components/Modal/ModalServiceInitializer";
import GlobalLoadingIndicator from "@/components/GlobalLoadingIndicator/GlobalLoadingIndicator";
import {ToastServiceInitializer} from "@/components/Toast/ToastServiceInitializer";
import {ToastProvider} from "@/components/Toast/ToastProvider";

const { publicRuntimeConfig } = getConfig()

export default function App({ Component, pageProps }: AppProps) {
    const [queryClient] = useState(() => new QueryClient())
    return (
        // Duplicates <body> classname because otherwise doesn't work, I don't know why
        <div className={jetBrainsMono.variable}>
            <Head>
                <link rel="icon" sizes="any" href={`${publicRuntimeConfig.basePath}/favicon.ico`} />
            </Head>
            <QueryClientProvider client={queryClient}>
                <HydrationBoundary state={pageProps.dehydratedState}>
                    <ToastProvider>
                        <ModalProvider>
                            <ToastServiceInitializer/>
                            <ModalServiceInitializer/>
                            <GlobalLoadingIndicator/>
                            <Component {...pageProps}/>
                        </ModalProvider>
                    </ToastProvider>
                </HydrationBoundary>
            </QueryClientProvider>
        </div>
    );
}
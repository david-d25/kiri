import type { AppProps } from 'next/app'

import '../styles/globals.scss'
import Head from "next/head";
import getConfig from 'next/config'
import {HydrationBoundary, QueryClient, QueryClientProvider} from "@tanstack/react-query";
import {useState} from "react";

const { publicRuntimeConfig } = getConfig()

export default function App({ Component, pageProps }: AppProps) {
    const [queryClient] = useState(() => new QueryClient())
    return (
        <>
            <Head>
                <link rel="icon" sizes="any" href={`${publicRuntimeConfig.basePath}/favicon.ico`} />
            </Head>
            <QueryClientProvider client={queryClient}>
                <HydrationBoundary state={pageProps.dehydratedState}>
                    <Component {...pageProps} />
                </HydrationBoundary>
            </QueryClientProvider>
        </>
    );
}
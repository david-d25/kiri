import React, { useState, useEffect, useRef } from "react";
import { useRouter } from "next/router";

import styles from "./GlobalLoadingIndicator.module.scss";

import LoadingDotsIcon from "@/icons/loading-dots.svg";

const DISPLAY_DELAY_MS = 200;

export default function GlobalLoadingIndicator() {
    const router = useRouter();
    const [mounted, setMounted] = useState(false); // To avoid hydration issues
    const [visible, setVisible] = useState(false);
    const timerRef = useRef<NodeJS.Timeout | null>(null);

    useEffect(() => {
        setMounted(true);
    }, []);

    useEffect(() => {
        const onStart = () => {
            timerRef.current = setTimeout(() => setVisible(true), DISPLAY_DELAY_MS);
        };
        const onEnd = () => {
            if (timerRef.current) {
                clearTimeout(timerRef.current);
                timerRef.current = null;
            }
            setVisible(false);
        };

        router.events.on("routeChangeStart", onStart);
        router.events.on("routeChangeComplete", onEnd);
        router.events.on("routeChangeError", onEnd);

        return () => {
            router.events.off("routeChangeStart", onStart);
            router.events.off("routeChangeComplete", onEnd);
            router.events.off("routeChangeError", onEnd);
        };
    }, [router.events]);

    if (!mounted || !visible) return null;

    return (
        <div className={styles.root}>
            <LoadingDotsIcon className={styles.icon}/>
            <span className={styles.text}>Загрузка…</span>
        </div>
    );
}
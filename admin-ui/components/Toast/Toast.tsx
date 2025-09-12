import React, { useEffect, useState, useRef } from 'react';
import styles from './Toast.module.scss';

export type ToastType = 'success' | 'error' | 'warning' | 'info';

export type ToastProps = {
    id: string;
    type: ToastType;
    title?: string;
    message: string;
    duration?: number;
    onRemove: (id: string) => void;
    showCloseButton?: boolean;
    showProgressBar?: boolean;
};

const TOAST_ICONS = {
    success: '✓',
    error: '✕',
    warning: '⚠',
    info: 'ℹ'
};

export default function Toast({
                                  id,
                                  type,
                                  title,
                                  message,
                                  duration = 5000,
                                  onRemove,
                                  showCloseButton = true,
                                  showProgressBar = true,
                              }: ToastProps) {
    const [isRemoving, setIsRemoving] = useState(false);
    const [progress, setProgress] = useState(100);
    const progressIntervalRef = useRef<NodeJS.Timeout | null>(null);
    const timeoutRef = useRef<NodeJS.Timeout | null>(null);
    const startTimeRef = useRef<number>(Date.now());

    const handleRemove = () => {
        if (isRemoving) return;

        setIsRemoving(true);
        // Wait for animation to complete before actually removing
        setTimeout(() => onRemove(id), 200);
    };

    useEffect(() => {
        if (duration <= 0) return;

        startTimeRef.current = Date.now();

        // Update progress bar
        if (showProgressBar) {
            progressIntervalRef.current = setInterval(() => {
                const elapsed = Date.now() - startTimeRef.current;
                const remaining = Math.max(0, duration - elapsed);
                const progressPercent = (remaining / duration) * 100;
                setProgress(progressPercent);

                if (remaining <= 0) {
                    if (progressIntervalRef.current) {
                        clearInterval(progressIntervalRef.current);
                    }
                }
            }, 50);
        }

        // Auto remove after duration
        timeoutRef.current = setTimeout(() => {
            handleRemove();
        }, duration);

        return () => {
            if (progressIntervalRef.current) {
                clearInterval(progressIntervalRef.current);
            }
            if (timeoutRef.current) {
                clearTimeout(timeoutRef.current);
            }
        };
    }, [duration, showProgressBar, id]);

    const handleMouseEnter = () => {
        // Pause auto-removal on hover
        if (timeoutRef.current) {
            clearTimeout(timeoutRef.current);
        }
        if (progressIntervalRef.current) {
            clearInterval(progressIntervalRef.current);
        }
    };

    const handleMouseLeave = () => {
        // Resume auto-removal when not hovering
        if (duration <= 0) return;

        const elapsed = Date.now() - startTimeRef.current;
        const remaining = Math.max(0, duration - elapsed);

        if (remaining > 0) {
            if (showProgressBar) {
                progressIntervalRef.current = setInterval(() => {
                    const newElapsed = Date.now() - startTimeRef.current;
                    const newRemaining = Math.max(0, duration - newElapsed);
                    const progressPercent = (newRemaining / duration) * 100;
                    setProgress(progressPercent);

                    if (newRemaining <= 0) {
                        if (progressIntervalRef.current) {
                            clearInterval(progressIntervalRef.current);
                        }
                    }
                }, 50);
            }

            timeoutRef.current = setTimeout(() => {
                handleRemove();
            }, remaining);
        }
    };

    return (
        <div
            className={`${styles.toast} ${styles[type]} ${isRemoving ? styles.removing : ''}`}
            onMouseEnter={handleMouseEnter}
            onMouseLeave={handleMouseLeave}
        >
            <div className={styles.toastIcon}>
                {TOAST_ICONS[type]}
            </div>

            <div className={styles.toastContent}>
                {title && (
                    <div className={styles.toastTitle}>
                        {title}
                    </div>
                )}
                <div className={styles.toastMessage}>
                    {message}
                </div>
            </div>

            {showCloseButton && (
                <button
                    className={styles.toastCloseButton}
                    onClick={handleRemove}
                    aria-label="Закрыть уведомление"
                >
                    ×
                </button>
            )}

            {showProgressBar && duration > 0 && (
                <div
                    className={styles.progressBar}
                    style={{ width: `${progress}%` }}
                />
            )}
        </div>
    );
}
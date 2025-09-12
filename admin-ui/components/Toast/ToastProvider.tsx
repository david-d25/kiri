import React, { createContext, useContext, useState, useCallback, useRef } from 'react';
import { createPortal } from 'react-dom';
import Toast, { ToastType } from './Toast';
import styles from './Toast.module.scss';

export type ToastConfig = {
    type: ToastType;
    title?: string;
    message: string;
    duration?: number;
    showCloseButton?: boolean;
    showProgressBar?: boolean;
};

type ToastInstance = ToastConfig & {
    id: string;
};

type ToastContextType = {
    showToast: (config: ToastConfig) => string;
    removeToast: (id: string) => void;
    clearAllToasts: () => void;

    // Convenience methods
    success: (message: string, options?: Partial<ToastConfig>) => string;
    error: (message: string, options?: Partial<ToastConfig>) => string;
    warning: (message: string, options?: Partial<ToastConfig>) => string;
    info: (message: string, options?: Partial<ToastConfig>) => string;
};

const ToastContext = createContext<ToastContextType | null>(null);

export function useToastService() {
    const context = useContext(ToastContext);
    if (!context) {
        throw new Error('useToastService must be used within ToastProvider');
    }
    return context;
}

export function ToastProvider({ children }: { children: React.ReactNode }) {
    const [toasts, setToasts] = useState<ToastInstance[]>([]);
    const toastIdRef = useRef(0);

    const showToast = useCallback((config: ToastConfig): string => {
        const id = `toast-${toastIdRef.current++}`;

        const newToast: ToastInstance = {
            id,
            ...config,
        };

        setToasts(prev => [...prev, newToast]);
        return id;
    }, []);

    const removeToast = useCallback((id: string) => {
        setToasts(prev => prev.filter(toast => toast.id !== id));
    }, []);

    const clearAllToasts = useCallback(() => {
        setToasts([]);
    }, []);

    // Convenience methods for different toast types
    const success = useCallback((message: string, options: Partial<ToastConfig> = {}) => {
        return showToast({
            type: 'success',
            message,
            ...options,
        });
    }, [showToast]);

    const error = useCallback((message: string, options: Partial<ToastConfig> = {}) => {
        return showToast({
            type: 'error',
            message,
            duration: 8000, // Longer duration for errors
            ...options,
        });
    }, [showToast]);

    const warning = useCallback((message: string, options: Partial<ToastConfig> = {}) => {
        return showToast({
            type: 'warning',
            message,
            duration: 7000, // Slightly longer for warnings
            ...options,
        });
    }, [showToast]);

    const info = useCallback((message: string, options: Partial<ToastConfig> = {}) => {
        return showToast({
            type: 'info',
            message,
            ...options,
        });
    }, [showToast]);

    const contextValue: ToastContextType = {
        showToast,
        removeToast,
        clearAllToasts,
        success,
        error,
        warning,
        info,
    };

    return (
        <ToastContext.Provider value={contextValue}>
            {children}
            {toasts.length > 0 && createPortal(
                <div className={styles.toastContainer}>
                    {toasts.map((toast) => (
                        <Toast
                            key={toast.id}
                            {...toast}
                            onRemove={removeToast}
                        />
                    ))}
                </div>,
                document.body
            )}
        </ToastContext.Provider>
    );
}
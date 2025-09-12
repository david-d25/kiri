import { ToastConfig } from "@/components/Toast/ToastProvider";

let globalToastService: any = null;

export function setGlobalToastService(service: any) {
    globalToastService = service;
}

export const toastService = {
    show(config: ToastConfig): string {
        if (!globalToastService) {
            throw new Error('Toast service not initialized. Make sure ToastProvider is mounted.');
        }
        return globalToastService.showToast(config);
    },

    success(message: string, options?: Partial<ToastConfig>): string {
        if (!globalToastService) {
            throw new Error('Toast service not initialized. Make sure ToastProvider is mounted.');
        }
        return globalToastService.success(message, options);
    },

    error(message: string, options?: Partial<ToastConfig>): string {
        if (!globalToastService) {
            throw new Error('Toast service not initialized. Make sure ToastProvider is mounted.');
        }
        return globalToastService.error(message, options);
    },

    warning(message: string, options?: Partial<ToastConfig>): string {
        if (!globalToastService) {
            throw new Error('Toast service not initialized. Make sure ToastProvider is mounted.');
        }
        return globalToastService.warning(message, options);
    },

    info(message: string, options?: Partial<ToastConfig>): string {
        if (!globalToastService) {
            throw new Error('Toast service not initialized. Make sure ToastProvider is mounted.');
        }
        return globalToastService.info(message, options);
    },

    remove(id: string): void {
        if (!globalToastService) {
            throw new Error('Toast service not initialized. Make sure ToastProvider is mounted.');
        }
        globalToastService.removeToast(id);
    },

    clearAll(): void {
        if (!globalToastService) {
            throw new Error('Toast service not initialized. Make sure ToastProvider is mounted.');
        }
        globalToastService.clearAllToasts();
    }
};
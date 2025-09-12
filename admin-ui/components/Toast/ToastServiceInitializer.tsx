import { useEffect } from 'react';
import { useToastService } from './ToastProvider';
import { setGlobalToastService } from "../../services/ToastService";

export function ToastServiceInitializer() {
    const toastService = useToastService();

    useEffect(() => {
        setGlobalToastService(toastService);
    }, [toastService]);

    return null;
}
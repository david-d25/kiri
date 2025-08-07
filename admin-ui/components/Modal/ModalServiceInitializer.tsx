import { useEffect } from 'react';
import { useModalService } from './ModalProvider';
import { setGlobalModalService } from "../../services/ModalService";

export function ModalServiceInitializer() {
    const modalService = useModalService();

    useEffect(() => {
        setGlobalModalService(modalService);
    }, [modalService]);

    return null;
}
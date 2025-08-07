import React from 'react';
import {ModalComponentProps} from "../components/Modal/ModalProvider";

let globalModalService: any = null;

export function setGlobalModalService(service: any) {
    globalModalService = service;
}

export const modalService = {
    open<T = any, P = {}>(config: {
        title?: string;
        content: React.ComponentType<ModalComponentProps<T> & P>;
        props?: P;
        closeOnBackdropClick?: boolean;
        closeOnEscape?: boolean;
    }): Promise<T | null> {
        if (!globalModalService) {
            throw new Error('Modal service not initialized. Make sure ModalProvider is mounted.');
        }
        return globalModalService.openModal(config);
    }
};
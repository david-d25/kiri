import React, { createContext, useContext, useState, useCallback, useRef } from 'react';
import Modal from './Modal';

export type ModalComponentProps<T = any> = {
    onSubmit: (data: T) => void;
    onCancel: () => void;
};

type ModalInstance = {
    id: string;
    title?: string;
    content: React.ComponentType<any>;
    props?: any;
    closeOnBackdropClick?: boolean;
    closeOnEscape?: boolean;
    resolve: (value: any) => void;
};

type ModalContextType = {
    openModal: <T = any, P = {}>(config: {
        title?: string;
        content: React.ComponentType<ModalComponentProps<T> & P>;
        props?: P;
        closeOnBackdropClick?: boolean;
        closeOnEscape?: boolean;
    }) => Promise<T | null>;
};

const ModalContext = createContext<ModalContextType | null>(null);

export function useModalService() {
    const context = useContext(ModalContext);
    if (!context) {
        throw new Error('useModalService must be used within ModalProvider');
    }
    return context;
}

export function ModalProvider({ children }: { children: React.ReactNode }) {
    const [modals, setModals] = useState<ModalInstance[]>([]);
    const modalIdRef = useRef(0);

    const openModal = useCallback(<T = any, P = {}>(config: {
        title?: string;
        content: React.ComponentType<ModalComponentProps<T> & P>;
        props?: P;
        closeOnBackdropClick?: boolean;
        closeOnEscape?: boolean;
    }): Promise<T | null> => {
        return new Promise<T | null>((resolve) => {
            const id = `modal-${modalIdRef.current++}`;

            setModals(prev => [...prev, {
                id,
                title: config.title,
                content: config.content as React.ComponentType<any>,
                props: config.props,
                closeOnBackdropClick: config.closeOnBackdropClick,
                closeOnEscape: config.closeOnEscape,
                resolve,
            }]);
        });
    }, []);

    const closeModal = useCallback((id: string) => {
        setModals(prev => prev.filter(modal => modal.id !== id));
    }, []);

    return (
        <ModalContext.Provider value={{ openModal }}>
            {children}
            {modals.map((modal) => {
                const { id, content: Content, resolve, props: customProps, ...modalProps } = modal;

                const handleSubmit = (data: any) => {
                    resolve(data);
                    closeModal(id);
                };

                const handleCancel = () => {
                    resolve(null);
                    closeModal(id);
                };

                return (
                    <Modal
                        key={id}
                        isOpen={true}
                        onClose={handleCancel}
                        title={modalProps.title}
                        closeOnBackdropClick={modalProps.closeOnBackdropClick}
                        closeOnEscape={modalProps.closeOnEscape}
                    >
                        <Content
                            onSubmit={handleSubmit}
                            onCancel={handleCancel}
                            {...(customProps || {})}
                        />
                    </Modal>
                );
            })}
        </ModalContext.Provider>
    );
}
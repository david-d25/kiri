import React, { useEffect, useRef } from 'react';
import { createPortal } from 'react-dom';
import styles from './Modal.module.scss';

export type ModalProps = {
    isOpen: boolean;
    onClose: () => void;
    title?: string;
    children: React.ReactNode;
    footer?: React.ReactNode;
    closeOnBackdropClick?: boolean;
    closeOnEscape?: boolean;
};

export default function Modal({
  isOpen,
  onClose,
  title,
  children,
  footer,
  closeOnBackdropClick = true,
  closeOnEscape = true,
}: ModalProps) {
    const modalRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        if (!isOpen) return;

        const handleEscape = (e: KeyboardEvent) => {
            if (e.key === 'Escape' && closeOnEscape) {
                onClose();
            }
        };

        document.addEventListener('keydown', handleEscape);
        document.body.style.overflow = 'hidden';

        return () => {
            document.removeEventListener('keydown', handleEscape);
            document.body.style.overflow = '';
        };
    }, [isOpen, onClose, closeOnEscape]);

    useEffect(() => {
        if (isOpen && modalRef.current) {
            // Focus the modal for keyboard navigation
            modalRef.current.focus();
        }
    }, [isOpen]);

    if (!isOpen) return null;

    const handleBackdropClick = (e: React.MouseEvent) => {
        if (e.target === e.currentTarget && closeOnBackdropClick) {
            onClose();
        }
    };

    return createPortal(
        <div className={styles.modalBackdrop} onClick={handleBackdropClick}>
            <div
                className={styles.modal}
                ref={modalRef}
                tabIndex={-1}
                onClick={(e) => e.stopPropagation()}
            >
                {title && (
                    <div className={styles.modalHeader}>
                        <h2 className={styles.modalTitle}>{title}</h2>
                        <button
                            className={styles.modalCloseButton}
                            onClick={onClose}
                            aria-label="Close modal"
                        >
                            ×
                        </button>
                    </div>
                )}
                <div className={styles.modalContent}>
                    {children}
                </div>
                {footer && (
                    <div className={styles.modalFooter}>
                        {footer}
                    </div>
                )}
            </div>
        </div>,
        document.body
    );
}
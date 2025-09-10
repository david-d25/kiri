import React, { useEffect, useRef } from 'react';
import { createPortal } from 'react-dom';
import styles from './Modal.module.scss';
import Button from "@/components/Button/Button";

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
    const backdropMouseDown = useRef(false);

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

    const handleBackdropMouseUp = (e: React.MouseEvent) => {
        if (e.target === e.currentTarget && closeOnBackdropClick) {
            if (backdropMouseDown.current) {
                onClose();
            }
        }
        backdropMouseDown.current = false;
    };

    const handleBackdropMouseDown = (e: React.MouseEvent) => {
        backdropMouseDown.current = !!(e.target === e.currentTarget && closeOnBackdropClick);
    }

    return createPortal(
        <div className={styles.modalBackdrop} onMouseUp={handleBackdropMouseUp} onMouseDown={handleBackdropMouseDown}>
            <div
                className={styles.modal}
                ref={modalRef}
                tabIndex={-1}
                onClick={(e) => e.stopPropagation()}
            >
                {title && (
                    <div className={styles.modalHeader}>
                        <h2 className={styles.modalTitle}>{title}</h2>
                        <Button className={styles.modalCloseButton} onClick={onClose} lightweight>
                            ×
                        </Button>
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
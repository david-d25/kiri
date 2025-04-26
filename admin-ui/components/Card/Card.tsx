import React from "react";
import styles from './Card.module.scss';

type Props = {
    children: React.ReactNode;
    title?: string;
    footer?: React.ReactNode;
};

export default function Card(
    {
        children,
        title,
        footer
    }: Props
) {
    return (
        <div className={styles.card}>
            {title && <div className={styles.cardHeader}>{title}</div>}
            <div className={styles.cardContent}>
                {children}
            </div>
            {footer && <div className={styles.cardFooter}>{footer}</div>}
        </div>
    );
}
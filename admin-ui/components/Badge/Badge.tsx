import React from "react";
import styles from './Badge.module.scss';

type BadgeType = 'default' | 'primary' | 'success' | 'warning' | 'error';

type Props = {
    children: React.ReactNode;
    type?: BadgeType;
    count?: number;
    showZero?: boolean;
    dot?: boolean;
    max?: number;
};

export default function Badge(
    {
        children,
        type = 'default',
        count,
        showZero = false,
        dot = false,
        max = 99
    }: Props
) {
    const hasCount = count !== undefined;
    const showBadge = dot || (hasCount && (count > 0 || showZero));

    const getDisplayCount = () => {
        if (count === undefined) return '';
        if (count > max) return `${max}+`;
        return count.toString();
    };

    return (
        <div className={styles.badgeContainer}>
            {children}
            {showBadge && (
                <span
                    className={styles.badge}
                    data-type={type}
                    data-dot={dot}
                >
                    {!dot && getDisplayCount()}
                </span>
            )}
        </div>
    );
}
import React, { useState, useRef } from "react";
import styles from './Tooltip.module.scss';

type Props = {
    children: React.ReactNode;
    content: string;
    position?: 'top' | 'bottom' | 'left' | 'right';
};

export default function Tooltip(
    {
        children,
        content,
        position = 'top'
    }: Props
) {
    const [isVisible, setIsVisible] = useState(false);
    const timeoutRef = useRef<NodeJS.Timeout | null>(null);

    const showTooltip = () => {
        if (timeoutRef.current) {
            clearTimeout(timeoutRef.current);
        }
        setIsVisible(true);
    };

    const hideTooltip = () => {
        timeoutRef.current = setTimeout(() => {
            setIsVisible(false);
        }, 100);
    };

    return (
        <div
            className={styles.tooltipContainer}
            onMouseEnter={showTooltip}
            onMouseLeave={hideTooltip}
            onFocus={showTooltip}
            onBlur={hideTooltip}
        >
            {children}
            {isVisible && (
                <div className={`${styles.tooltip} ${styles[position]}`}>
                    {content}
                </div>
            )}
        </div>
    );
}
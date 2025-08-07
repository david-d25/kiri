import React, { useState, useRef } from "react";
import styles from './Tooltip.module.scss';
import {classnames} from "@/lib/classnames";

type Props = {
    children: React.ReactNode;
    content: React.ReactNode;
    position?: 'top' | 'bottom' | 'left' | 'right';
    delay?: number;
    show?: 'auto' | boolean;
    className?: string
};

export default function Tooltip(
    {
        children,
        content,
        position = 'top',
        delay = 500,
        show = 'auto',
        className
    }: Props
) {
    const [autoIsVisible, setAutoIsVisible] = useState(false);
    const timeoutRef = useRef<NodeJS.Timeout | null>(null);

    const showTooltip = () => {
        if (timeoutRef.current) {
            clearTimeout(timeoutRef.current);
        }
        timeoutRef.current = setTimeout(() => {
            setAutoIsVisible(true);
        }, delay);
    };

    const hideTooltip = () => {
        if (timeoutRef.current) {
            clearTimeout(timeoutRef.current);
        }
        setAutoIsVisible(false);
    };

    const visible = show === 'auto' ? autoIsVisible : show;

    return (
        <div
            className={classnames(styles.tooltipContainer, className)}
            onMouseEnter={showTooltip}
            onMouseLeave={hideTooltip}
            onFocus={showTooltip}
            onBlur={hideTooltip}
        >
            {children}
            {visible && (
                <div className={`${styles.tooltip} ${styles[position]}`}>
                    {content}
                </div>
            )}
        </div>
    );
}
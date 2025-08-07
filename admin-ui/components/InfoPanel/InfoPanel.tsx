import React from "react";

import styles from './InfoPanel.module.scss';
import InfoIcon from './icons/info.svg';
import ErrorIcon from './icons/error.svg';
import WarningIcon from './icons/warning.svg';
import {classnames} from "@/lib/classnames";

type InfoPanelType = 'info' | 'error' | 'warning';

type Props = {
    children: React.ReactNode;
    type?: InfoPanelType;
    borderless?: boolean;
    className?: string;
};

export default function InfoPanel(
    {
        children,
        type = 'info',
        borderless = false,
        className
    }: Props
) {
    return (
        <div className={classnames(styles.infoPanel, className)} data-type={type} data-borderless={borderless}>
            {renderIcon(type)}
            {children}
        </div>
    )
}

function renderIcon(type: InfoPanelType) {
    switch (type) {
        case 'info':
            return <InfoIcon className={styles.icon}/>;
        case 'error':
            return <ErrorIcon className={styles.icon}/>;
        case 'warning':
            return <WarningIcon className={styles.icon}/>;
        default:
            return null;
    }
}
import React from "react";

import styles from './InfoPanel.module.scss';
import InfoIcon from './icons/info.svg';
import ErrorIcon from './icons/error.svg';

type InfoPanelType = 'info' | 'error';

type Props = {
    children: React.ReactNode;
    type?: InfoPanelType;
};

export default function InfoPanel(
    {
        children,
        type = 'info'
    }: Props
) {
    return (
        <div className={styles.infoPanel} data-type={type}>
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
        default:
            return null;
    }
}
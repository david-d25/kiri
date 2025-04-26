import React from "react";
import styles from './Divider.module.scss';

type Props = {
    text?: string;
    direction?: 'horizontal' | 'vertical';
};

export default function Divider(
    {
        text,
        direction = 'horizontal'
    }: Props
) {
    return (
        <div
            className={styles.divider}
            data-direction={direction}
        >
            {text && <span className={styles.dividerText}>{text}</span>}
        </div>
    );
}
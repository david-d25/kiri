import React from "react";
import styles from './LoadingOverlay.module.scss';
import {classnames} from "../../lib/classnames";

type Props = {
    children: React.ReactNode;
    loading: boolean;
    spinnerSize?: 'small' | 'medium' | 'large';
    blur?: boolean;
    message?: string;
};

export default function LoadingOverlay(
    {
        children,
        loading,
        spinnerSize = 'medium',
        blur = false,
        message
    }: Props
) {
    return (
        <div className={styles.container} data-active={loading}>
            <div className={styles.children}>
                {children}
            </div>

            {loading && (
                <div className={styles.overlay} data-blur={blur}>
                    <div className={styles.content}>
                        <div className={classnames(styles.spinner, styles[spinnerSize])}>
                            <div className={styles.spinnerInner}></div>
                        </div>

                        {message && (
                            <div className={styles.message}>{message}</div>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}
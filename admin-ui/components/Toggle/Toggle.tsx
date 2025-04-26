import React from "react";
import styles from './Toggle.module.scss';

type Props = {
    checked: boolean;
    label?: string;
    disabled?: boolean;
    onChange: (checked: boolean) => void;
};

export default function Toggle(
    {
        checked,
        label,
        disabled = false,
        onChange
    }: Props
) {
    const handleChange = () => {
        if (!disabled) {
            onChange(!checked);
        }
    };

    return (
        <div className={styles.toggleContainer}>
            {label && <span className={styles.label}>{label}</span>}
            <div
                className={styles.toggle}
                data-checked={checked}
                data-disabled={disabled}
                onClick={handleChange}
                tabIndex={disabled ? undefined : 0}
                onKeyDown={(e) => {
                    if ((e.key === 'Enter' || e.key === ' ') && !disabled) {
                        e.preventDefault();
                        onChange(!checked);
                    }
                }}
            >
                <div className={styles.toggleHandle} />
            </div>
        </div>
    );
}
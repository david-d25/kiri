import React from "react";
import styles from './Checkbox.module.scss';
import CheckIcon from './icons/check.svg';

type Props = {
    label: string;
    checked: boolean;
    disabled?: boolean;
    onChange: (checked: boolean) => void;
};

export default function Checkbox(
    {
        label,
        checked,
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
        <div
            className={styles.checkbox}
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
            <div className={styles.checkboxBox}>
                {checked && <CheckIcon className={styles.checkIcon} />}
            </div>
            <span className={styles.label}>{label}</span>
        </div>
    );
}
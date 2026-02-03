import React from "react";
import styles from './Checkbox.module.scss';
import CheckIcon from './icons/check.svg';
import {classnames} from "@/lib/classnames";

type Props = {
    label: React.ReactNode;
    checked: boolean;
    disabled?: boolean;
    muted?: boolean;
    onChange: (checked: boolean) => void;
    className?: string;
};

export default function Checkbox(
    {
        label,
        checked,
        disabled = false,
        muted = false,
        onChange,
        className,
    }: Props
) {
    const handleChange = (e: React.MouseEvent) => {
        e.stopPropagation();
        if (!disabled) {
            onChange(!checked);
        }
    };

    return (
        <div
            className={classnames(styles.checkbox, className)}
            data-checked={checked}
            data-disabled={disabled}
            data-muted={muted}
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
import React, { ChangeEvent } from "react";
import styles from './TextArea.module.scss';

type Props = {
    value: string;
    onChange: (value: string) => void;
    placeholder?: string;
    label?: string;
    disabled?: boolean;
    maxLength?: number;
    rows?: number;
    error?: string;
    resizable?: boolean;
};

export default function TextArea(
    {
        value,
        onChange,
        placeholder = '',
        label,
        disabled = false,
        maxLength,
        rows = 4,
        error,
        resizable = true
    }: Props
) {
    const handleChange = (e: ChangeEvent<HTMLTextAreaElement>) => {
        onChange(e.target.value);
    };

    return (
        <div className={styles.textAreaContainer}>
            {label && <label className={styles.label}>{label}</label>}
            <textarea
                className={styles.textArea}
                value={value}
                onChange={handleChange}
                placeholder={placeholder}
                disabled={disabled}
                maxLength={maxLength}
                rows={rows}
                data-error={Boolean(error)}
                data-resizable={resizable}
            />
            {error && <div className={styles.errorMessage}>{error}</div>}
            {maxLength && (
                <div className={styles.charCount}>
                    {value.length}/{maxLength}
                </div>
            )}
        </div>
    );
}
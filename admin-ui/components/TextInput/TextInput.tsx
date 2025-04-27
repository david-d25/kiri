import React, { ChangeEvent } from "react";
import styles from './TextInput.module.scss';

type Props = {
    value: string;
    onChange: (value: string) => void;
    placeholder?: string;
    label?: string;
    disabled?: boolean;
    maxLength?: number;
    type?: 'text' | 'password' | 'email' | 'number';
    error?: string;
};

export default function TextInput(
    {
        value,
        onChange,
        placeholder = '',
        label,
        disabled = false,
        maxLength,
        type = 'text',
        error
    }: Props
) {
    const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
        onChange(e.target.value);
    };

    return (
        <div className={styles.inputContainer}>
            {label && <label className={styles.label}>{label}</label>}
            <input
                type={type}
                className={styles.input}
                value={value}
                onChange={handleChange}
                placeholder={placeholder}
                disabled={disabled}
                maxLength={maxLength}
                data-error={Boolean(error)}
            />
            {error && <div className={styles.errorMessage}>{error}</div>}
        </div>
    );
}
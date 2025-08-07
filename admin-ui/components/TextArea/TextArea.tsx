import React, { ChangeEvent } from "react";
import styles from './TextArea.module.scss';
import {classnames} from "@/lib/classnames";

type Props = {
    value: string;
    onChange: (value: string) => void;
    placeholder?: string;
    label?: string;
    disabled?: boolean;
    maxLength?: number;
    rows?: number;
    error?: string | null;
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

    const containerClassname = classnames({
        [styles.root]: true,
        [styles.disabled]: disabled
    });
    return (
        <div className={containerClassname}>
            {label && <label className={styles.label}>{label}</label>}
            <div className={styles.textAreaWrapper}>
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
                {maxLength && (
                    <div className={styles.charCount}>
                        {value.length}/{maxLength}
                    </div>
                )}
            </div>
            {error && <div className={styles.errorMessage}>{error}</div>}
        </div>
    );
}
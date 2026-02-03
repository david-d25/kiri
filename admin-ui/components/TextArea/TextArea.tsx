import React, { ChangeEvent } from "react";
import styles from './TextArea.module.scss';
import {classnames} from "@/lib/classnames";

type Props = {
    value: string;
    onChange: (value: string) => void;
    placeholder?: string;
    label?: React.ReactNode;
    disabled?: boolean;
    muted?: boolean;
    maxLength?: number;
    rows?: number;
    error?: string | null;
    resizable?: boolean;
    className?: string;
    autocomplete?: string;
    autocorrect?: string;
    spellcheck?: boolean;
    autocapitalize?: string;
};

export default function TextArea(props: Props) {
    const {
        value,
        onChange,
        placeholder = '',
        label,
        disabled = false,
        muted = false,
        maxLength,
        rows = 4,
        error,
        resizable = true,
        className
    } = props;

    const handleChange = (e: ChangeEvent<HTMLTextAreaElement>) => {
        onChange(e.target.value);
    };

    const containerClassname = classnames(className, {
        [styles.root]: true,
        [styles.disabled]: disabled,
        [styles.muted]: muted,
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
                    autoComplete={props.autocomplete}
                    autoCorrect={props.autocorrect}
                    spellCheck={props.spellcheck}
                    autoCapitalize={props.autocapitalize}
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
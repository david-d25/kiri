import React, {ChangeEvent, FocusEventHandler, KeyboardEventHandler} from "react";
import styles from './TextInput.module.scss';
import {classnames} from "@/lib/classnames";

type Props = {
    value: string;
    onChange: (value: string) => void
    onFocus?: FocusEventHandler;
    onBlur?: FocusEventHandler;
    onKeyDown?: KeyboardEventHandler;
    placeholder?: string;
    label?: string;
    disabled?: boolean;
    maxLength?: number;
    type?: 'text' | 'password' | 'email' | 'number';
    error?: string | null;
    className?: string;
    inputClassName?: string;
    ref?: React.Ref<HTMLInputElement>;
};

export default function TextInput(
    {
        value,
        onChange,
        onFocus,
        onBlur,
        onKeyDown,
        placeholder = '',
        label,
        disabled = false,
        maxLength,
        type = 'text',
        error,
        className,
        inputClassName,
        ref
    }: Props
) {
    const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
        onChange(e.target.value);
    };

    return (
        <div className={classnames(styles.inputContainer, className)}>
            {label && <label className={styles.label}>{label}</label>}
            <input
                type={type}
                className={classnames(styles.input, inputClassName)}
                value={value}
                onChange={handleChange}
                onFocus={onFocus}
                onBlur={onBlur}
                onKeyDown={onKeyDown}
                placeholder={placeholder}
                disabled={disabled}
                maxLength={maxLength}
                ref={ref}
                data-error={Boolean(error)}
            />
            {error && <div className={styles.errorMessage}>{error}</div>}
        </div>
    );
}
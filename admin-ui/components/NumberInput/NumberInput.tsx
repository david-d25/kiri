import React, {ChangeEvent, FocusEventHandler, KeyboardEvent, useState, useEffect} from "react";
import styles from './NumberInput.module.scss';
import {classnames} from "@/lib/classnames";
import {clamp} from "@/lib/mathUtils";

type Props = {
    value: number | null;
    onChange: (value: number | null) => void
    placeholder?: string;
    label?: React.ReactNode;
    disabled?: boolean;
    muted?: boolean;
    maxLength?: number;
    error?: string | null;
    className?: string;
    inputClassName?: string;
    ref?: React.Ref<HTMLInputElement>;
    step?: number | string;
    minValue?: number | null;
    maxValue?: number | null;
};

export default function NumberInput(props: Props) {
    const [ internalValue, setInternalValue ] = useState<string | null>('');
    const [ internalError, setInternalError ] = useState<string | null>(null);

    useEffect(() => {
        setInternalValue(props.value?.toString() || null)
    }, [props.value]);

    const handleKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
        if (e.key === 'Enter') {
            handleValueSet(e.currentTarget.value);
        }
        if (e.key === 'Escape') {
            setInternalValue(props.value?.toString() || null);
        }
    }

    const handleBlur: FocusEventHandler<HTMLInputElement> = (e) => {
        handleValueSet(e.target.value);
    }

    const handleFocus = (e: ChangeEvent<HTMLInputElement>) => {
        e.target.select();
    }

    function handleValueSet(value: string) {
        if (value.trim() === '') {
            props.onChange(null);
            setInternalError(null);
            return;
        }
        if (isNaN(+value)) {
            setInternalError('Это должно быть число');
            return;
        } else {
            props.onChange(clamp(+value, props.minValue ?? null, props.maxValue ?? null));
            setInternalError(null);
        }
    }

    const anyError = internalError || props.error;

    return (
        <div className={classnames(styles.root, props.className)} data-muted={props.muted}>
            { props.label && <label className={styles.label} data-disabled={props.disabled}>{props.label}</label> }
            <input
                type={'number'}
                className={classnames(styles.input, props.inputClassName)}
                value={internalValue || ''}
                onChange={e => setInternalValue(e.target.value)}
                onKeyDown={handleKeyDown}
                onFocus={handleFocus}
                onBlur={handleBlur}
                placeholder={props.placeholder}
                disabled={props.disabled}
                maxLength={props.maxLength}
                ref={props.ref}
                step={props.step}
                data-error={Boolean(props.error)}
            />
            {anyError && <div className={styles.errorMessage}>{anyError}</div>}
        </div>
    );
}
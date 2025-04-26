import React from "react";
import styles from './Radio.module.scss';

type RadioOption = {
    label: string;
    value: string;
};

type Props = {
    options: RadioOption[];
    selectedValue: string;
    name: string;
    disabled?: boolean;
    onChange: (value: string) => void;
};

export default function Radio(
    {
        options,
        selectedValue,
        name,
        disabled = false,
        onChange
    }: Props
) {
    return (
        <div className={styles.radioGroup}>
            {options.map((option) => (
                <div
                    key={option.value}
                    className={styles.radioOption}
                    data-selected={selectedValue === option.value}
                    data-disabled={disabled}
                    onClick={() => {
                        if (!disabled) {
                            onChange(option.value);
                        }
                    }}
                    tabIndex={disabled ? undefined : 0}
                    onKeyDown={(e) => {
                        if ((e.key === 'Enter' || e.key === ' ') && !disabled) {
                            e.preventDefault();
                            onChange(option.value);
                        }
                    }}
                >
                    <div className={styles.radioButton}>
                        {selectedValue === option.value && <div className={styles.radioButtonInner} />}
                    </div>
                    <span className={styles.label}>{option.label}</span>
                </div>
            ))}
        </div>
    );
}
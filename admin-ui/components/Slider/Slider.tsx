import React, { ChangeEvent } from "react";
import styles from './Slider.module.scss';

type Props = {
    value: number;
    min?: number;
    max?: number;
    step?: number;
    disabled?: boolean;
    onChange: (value: number) => void;
};

export default function Slider(
    {
        value,
        min = 0,
        max = 100,
        step = 1,
        disabled = false,
        onChange
    }: Props
) {
    const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
        onChange(Number(e.target.value));
    };

    return (
        <div className={styles.sliderContainer}>
            <input
                type="range"
                className={styles.slider}
                value={value}
                min={min}
                max={max}
                step={step}
                disabled={disabled}
                onChange={handleChange}
            />
            <div className={styles.value}>{value}</div>
        </div>
    );
}
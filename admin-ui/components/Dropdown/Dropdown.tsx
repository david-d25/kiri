import React, { useState, useRef, useEffect } from "react";
import styles from './Dropdown.module.scss';
import ArrowDownIcon from './icons/arrow-down.svg';
import {classnames} from "@/lib/classnames";

type DropdownOption<T> = {
    label: string;
    value: T;
};

type Props<T> = {
    options: DropdownOption<T>[];
    selectedValue: T;
    placeholder?: string;
    disabled?: boolean;
    onChange: (value: T) => void;
    className?: string;
    label?: string;
};

export default function Dropdown<T>(
    {
        options,
        selectedValue,
        placeholder = 'Select an option',
        disabled = false,
        onChange,
        className,
        label
    }: Props<T>
) {
    const [isOpen, setIsOpen] = useState(false);
    const dropdownRef = useRef<HTMLDivElement>(null);

    const selectedOption = options.find(option => {
        if (typeof selectedValue === 'object'
            && selectedValue !== null
            && typeof option.value === 'object'
            && option.value !== null
        ) {
            // Deep equality check for objects
            return JSON.stringify(option.value) === JSON.stringify(selectedValue);
        }
        return option.value === selectedValue;
    });

    const toggleDropdown = () => {
        if (!disabled) {
            setIsOpen(!isOpen);
        }
    };

    const handleOptionClick = (value: T) => {
        onChange(value);
        setIsOpen(false);
    };

    // Close dropdown when clicking outside
    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
                setIsOpen(false);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, []);

    // Handle keyboard navigation
    const handleKeyDown = (e: React.KeyboardEvent) => {
        if (disabled) return;

        switch (e.key) {
            case 'Enter':
            case ' ':
                e.preventDefault();
                toggleDropdown();
                break;
            case 'Escape':
                setIsOpen(false);
                break;
            case 'ArrowDown':
                if (isOpen) {
                    e.preventDefault();
                    const currentIndex = options.findIndex(option => option.value === selectedValue);
                    const nextIndex = (currentIndex + 1) % options.length;
                    onChange(options[nextIndex].value);
                }
                break;
            case 'ArrowUp':
                if (isOpen) {
                    e.preventDefault();
                    const currentIndex = options.findIndex(option => option.value === selectedValue);
                    const prevIndex = (currentIndex - 1 + options.length) % options.length;
                    onChange(options[prevIndex].value);
                }
                break;
        }
    };

    return (
        <div
            className={classnames(styles.dropdownContainer, className)}
            ref={dropdownRef}
            data-disabled={disabled}
        >
            {label && <label className={styles.label}>{label}</label>}
            <div
                className={styles.dropdownHeader}
                onClick={toggleDropdown}
                tabIndex={disabled ? undefined : 0}
                onKeyDown={handleKeyDown}
                data-open={isOpen}
            >
                <span className={styles.selectedOption}>
                    {selectedOption ? selectedOption.label : placeholder}
                </span>
                <ArrowDownIcon className={styles.arrowIcon} data-open={isOpen} />
            </div>

            {isOpen && (
                <div className={styles.dropdownOptions}>
                    {options.map((option, index) => (
                        <div
                            key={index}
                            className={styles.dropdownOption}
                            data-selected={option.value === selectedValue}
                            onClick={() => handleOptionClick(option.value)}
                        >
                            {option.label}
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}
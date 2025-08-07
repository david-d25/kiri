import styles from "./PageSelector.module.scss";
import {classnames} from "@/lib/classnames";
import ChevronLeftIcon from "@/icons/chevron-left.svg";
import ChevronRightIcon from "@/icons/chevron-right.svg";
import ChevronFirstIcon from "@/icons/chevron-first.svg";
import ChevronLastIcon from "@/icons/chevron-last.svg";
import {KeyboardEvent, useEffect, useState} from "react";

export type PageSelectorProps = {
    pages: number;
    currentPage: number;
    onPageChange: (page: number) => void;
    className?: string;
    disabled?: boolean;
}

export default function PageSelector(props: PageSelectorProps) {
    const firstDisabled = props.currentPage <= 0 || props.disabled;
    const lastDisabled = props.currentPage >= props.pages - 1 || props.disabled;
    const previousDisabled = props.currentPage <= 0 || props.disabled;
    const nextDisabled = props.currentPage >= props.pages - 1 || props.disabled;
    const inputDisabled = props.disabled || props.pages <= 1;
    const [ inputValue, setInputValue ] = useState('');
    useEffect(() => {
        setInputValue((props.currentPage + 1).toString())
    }, [props.currentPage]);

    function onInputKeyDown(event: KeyboardEvent<HTMLInputElement>) {
        if (props.disabled) {
            return;
        }
        if (event.key === "Enter") {
            const page = parseInt(inputValue, 10) - 1; // Convert to 0-based index
            if (isNaN(page)) {
                setInputValue((props.currentPage + 1).toString()); // Reset to current page if input is invalid
                return;
            }
            if (page >= 0 && page < props.pages) {
                props.onPageChange(page);
                setInputValue((page + 1).toString()); // Update input to reflect new page
            } else {
                setInputValue((props.currentPage + 1).toString()); // Reset to current page if out of bounds
            }
        } else if (event.key === "ArrowUp" || event.key === "ArrowDown") {
            // Prevent scrolling the page when using arrow keys
            event.preventDefault();
        } else if (event.key === "Escape") {
            setInputValue((props.currentPage + 1).toString()); // Reset input value on Escape
        }
    }

    function onFirstClick() {
        if (!firstDisabled) {
            props.onPageChange(0);
        }
    }
    function onLastClick() {
        if (!lastDisabled) {
            props.onPageChange(props.pages - 1);
        }
    }
    function onPreviousClick() {
        if (props.currentPage > 0) {
            props.onPageChange(props.currentPage - 1);
        }
    }
    function onNextClick() {
        if (props.currentPage < props.pages - 1) {
            props.onPageChange(props.currentPage + 1);
        }
    }
    return (
        <div className={classnames(styles.root, props.className)}>
            <button className={styles.button} disabled={firstDisabled} onClick={onFirstClick}>
                <ChevronFirstIcon className={styles.icon}/>
            </button>
            <button className={styles.button} disabled={previousDisabled} onClick={onPreviousClick}>
                <ChevronLeftIcon className={styles.icon}/>
            </button>
            <input
                className={styles.pageInput}
                disabled={inputDisabled}
                value={inputValue} // Display as 1-based index
                onChange={event => setInputValue(event.target.value)}
                onKeyDown={onInputKeyDown}
                onFocus={event => event.target.select()}
            />
            <button className={styles.button} disabled={nextDisabled} onClick={onNextClick}>
                <ChevronRightIcon className={styles.icon}/>
            </button>
            <button className={styles.button} disabled={lastDisabled} onClick={onLastClick}>
                <ChevronLastIcon className={styles.icon}/>
            </button>
            <span className={styles.totalPages}>
                {props.pages}
            </span>
        </div>
    );
}
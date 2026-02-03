import styles from "./MultiSelect.module.scss";

import React, {KeyboardEvent, ReactNode, useState} from "react";

import CrossIcon from "@/icons/cross.svg";
import {classnames} from "@/lib/classnames";

export interface MultiSelectProps<T> {
    possibleValues: T[];
    selectedValues: T[];
    onSelect: (item: T) => void;
    onDeselect: (item: T) => void;
    valueToSearchString: (item: T) => string;
    valueToIdentityKey: (item: T) => string | number;
    valueToItem?: (item: T) => React.ReactNode;
    label?: React.ReactNode;
    error?: string | null;
    placeholder?: string;
    className?: string;
    disabled?: boolean;
    muted?: boolean;
    maxItems?: number;
}

export default function MultiSelect<T>(props: MultiSelectProps<T>) {
    const [ searchTerm, setSearchTerm ] = useState("");
    const [ isInputFocused, setIsInputFocused ] = useState(false);
    const [ selectedIndex, setSelectedIndex ] = useState(0);
    // User removes the last item by tapping Backspace twice
    const [ lastItemRemoveConfirmation, setLastItemRemoveConfirmation ] = useState(false);

    const suggestions = props.possibleValues.filter(item =>
        !props.selectedValues.map(props.valueToIdentityKey).includes(props.valueToIdentityKey(item))
        && props.valueToSearchString(item).toLowerCase().includes(searchTerm.toLowerCase())
    ).slice(0, 100); // Limit to 100 suggestions

    const maxItemsReached = props.maxItems && props.selectedValues.length >= props.maxItems;
    const showSuggestions = !maxItemsReached
        && isInputFocused
        && (suggestions.length > 0 || searchTerm.length == 0)
        && props.possibleValues.length > 0;

    function onSelect(item: T) {
        if (props.disabled || props.selectedValues.includes(item) || maxItemsReached) {
            return; // Do not select if disabled or already selected
        }
        props.onSelect(item);
        setSearchTerm(""); // Clear search term after selection
        setIsInputFocused(false);
    }

    function onKeyPress(e: KeyboardEvent<HTMLInputElement>) {
        if (props.disabled) {
            return;
        }
        setLastItemRemoveConfirmation(false);
        if (e.key === "Enter" && showSuggestions) {
            const selectedItem = suggestions[selectedIndex];
            if (selectedItem) {
                onSelect(selectedItem);
            } else {
                // If no item is selected, just clear the search term
                setSearchTerm("");
            }
            e.preventDefault(); // Prevent form submission or other default behavior
        } else if (e.key === "Escape") {
            setSearchTerm(""); // Clear search term on Escape
        } else if (e.key === "Backspace" && searchTerm === "" && props.selectedValues.length > 0) {
            if (lastItemRemoveConfirmation) {
                // Remove the last selected item
                props.onDeselect(props.selectedValues[props.selectedValues.length - 1]);
                setLastItemRemoveConfirmation(false);
            } else {
                setLastItemRemoveConfirmation(true);
            }
        } else if (e.key === "ArrowDown") {
            e.preventDefault(); // Prevent default scrolling behavior
            if (selectedIndex < suggestions.length - 1) {
                setSelectedIndex(selectedIndex + 1);
            }
        } else if (e.key === "ArrowUp") {
            e.preventDefault(); // Prevent default scrolling behavior
            if (selectedIndex > 0) {
                setSelectedIndex(selectedIndex - 1);
            } else {
                setSelectedIndex(-1); // Reset to no selection
            }
        }

        if (e.key !== "ArrowDown" && e.key !== "ArrowUp") {
            setSelectedIndex(0);
        }
    }

    const rootClassname = classnames({
        [styles.root]: true,
        [styles.focused]: isInputFocused,
        [styles.disabled]: props.disabled,
        [styles.showsSuggestions]: showSuggestions,
        [props.className || ""]: !!props.className,
    })

    return (
        <div className={rootClassname} data-muted={Boolean(props.muted)}>
            { props.label && <label className={styles.label}>{props.label}</label> }
            <div className={styles.body} data-error={Boolean(props.error)}>
                { props.selectedValues.map((item, index) => {
                    const lastItem = index === props.selectedValues.length - 1;
                    const removeConfirmation = lastItem && lastItemRemoveConfirmation;
                    const className = classnames({
                        [styles.removeConfirmation]: removeConfirmation,
                    });
                    return (
                        <Item
                            className={className}
                            key={props.valueToIdentityKey(item)}
                            item={item}
                            renderItem={item => props.valueToItem?.(item) || props.valueToSearchString(item)}
                            onRemove={() => props.onDeselect(item)}
                            disabled={props.disabled}
                        />
                    )
                }) }
                { !maxItemsReached && (
                    <input
                        type='text'
                        className={styles.textInput}
                        value={searchTerm}
                        onChange={e => setSearchTerm(e.target.value)}
                        onFocus={() => setIsInputFocused(true)}
                        onBlur={() => setIsInputFocused(false)}
                        disabled={props.disabled}
                        placeholder={props.placeholder || ""}
                        onKeyDown={onKeyPress}
                    />
                ) }
                { showSuggestions && (
                    <div className={styles.suggestions}>
                        { suggestions.map((suggestion, index) => {
                            const isSelected = selectedIndex === index;
                            const className = classnames({
                                [styles.suggestionItem]: true,
                                [styles.focused]: isSelected,
                            });
                            return (
                                <div
                                    className={className}
                                    onMouseDown={() => onSelect(suggestion)}
                                    onMouseOver={() => setSelectedIndex(index)}
                                    key={index}
                                >
                                    {props.valueToItem?.(suggestion) || props.valueToSearchString(suggestion)}
                                </div>
                            )
                        }) }
                    </div>
                ) }
            </div>
            { props.error && <div className={styles.errorMessage}>{props.error}</div> }
        </div>
    )
}

interface ItemProps<T> {
    item: T
    renderItem: (item: T) => ReactNode;
    onRemove?: () => void;
    className?: string;
    disabled?: boolean;
}

function Item<T>(props: ItemProps<T>) {
    return (
        <div className={classnames(styles.item, props.className)}>
            <span className={styles.content}>
                {props.renderItem(props.item)}
            </span>
            <button className={styles.closeButton} disabled={props.disabled} onClick={props.onRemove}>
                <CrossIcon className={styles.closeButtonIcon}/>
            </button>
        </div>
    )
}
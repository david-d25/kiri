import styles from "./MultiSelect.module.scss";

import React, {KeyboardEvent, ReactNode, useState} from "react";

import CrossIcon from "@/icons/cross.svg";
import {classnames} from "@/lib/classnames";

export interface MultiSelectProps<T> {
    items: T[];
    selectedItems: T[];
    onSelect: (item: T) => void;
    onDeselect: (item: T) => void;
    itemToLabel: (item: T) => string;
    itemToIdentityKey: (item: T) => string | number;
    label?: string;
    placeholder?: string;
    className?: string;
    disabled?: boolean;
    maxItems?: number;
}

export default function MultiSelect<T>(props: MultiSelectProps<T>) {
    const [ searchTerm, setSearchTerm ] = useState("");
    const [ isInputFocused, setIsInputFocused ] = useState(false);
    const [ selectedIndex, setSelectedIndex ] = useState(0);
    // User removes the last item by tapping Backspace twice
    const [ lastItemRemoveConfirmation, setLastItemRemoveConfirmation ] = useState(false);

    const suggestions = props.items.filter(item =>
        !props.selectedItems.map(props.itemToIdentityKey).includes(props.itemToIdentityKey(item))
        && props.itemToLabel(item).toLowerCase().includes(searchTerm.toLowerCase())
    ).slice(0, 5); // Limit to 5 suggestions

    const maxItemsReached = props.maxItems && props.selectedItems.length >= props.maxItems;
    const showSuggestions = !maxItemsReached && searchTerm.length > 0 && isInputFocused && suggestions.length > 0;

    const rootClassname = classnames({
        [styles.root]: true,
        [styles.focused]: isInputFocused,
        [styles.disabled]: props.disabled,
        [styles.showsSuggestions]: showSuggestions,
        [props.className || ""]: !!props.className,
    })

    function onSelect(item: T) {
        if (props.disabled || props.selectedItems.includes(item) || maxItemsReached) {
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
        if (e.key === "Enter" && searchTerm.trim() !== "") {
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
        } else if (e.key === "Backspace" && searchTerm === "" && props.selectedItems.length > 0) {
            if (lastItemRemoveConfirmation) {
                // Remove the last selected item
                props.onDeselect(props.selectedItems[props.selectedItems.length - 1]);
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

    return (
        <div className={rootClassname}>
            { props.label && <label className={styles.label}>{props.label}</label> }
            <div className={styles.body}>
                { props.selectedItems.map((item, index) => {
                    const lastItem = index === props.selectedItems.length - 1;
                    const removeConfirmation = lastItem && lastItemRemoveConfirmation;
                    const className = classnames({
                        [styles.removeConfirmation]: removeConfirmation,
                    });
                    return (
                        <Item<T>
                            className={className}
                            key={props.itemToIdentityKey(item)}
                            item={item}
                            renderItem={props.itemToLabel}
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
            </div>
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
                                {props.itemToLabel(suggestion)}
                            </div>
                        )
                    }) }
                </div>
            ) }
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
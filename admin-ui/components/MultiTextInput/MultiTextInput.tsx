import s from "./MultiTextInput.module.scss";
import React, {useEffect, useRef, useState} from "react";
import Button from "@/components/Button/Button";
import TextInput from "@/components/TextInput/TextInput";
import AddIcon from "@/icons/add.svg";
import DeleteIcon from "@/icons/delete.svg";

type MultiTextInputProps = {
    items: string[];
    label?: string;
    maxItems?: number;
    disabled?: boolean;
    onAdd?: (string: string, error: (message: string) => void) => void;
    onDelete?: (item: string, index: number) => void;
}

export default function(props: MultiTextInputProps) {
    const [ adding, setAdding ] = useState(false);
    const [ textInput, setTextInput ] = useState("");
    const [ inputError, setInputError ] = useState<string | null>(null);
    const textInputRef = useRef<HTMLInputElement>(null);

    const maxItemsReached = props.maxItems !== undefined && props.items.length >= props.maxItems;

    useEffect(() => {
        if (adding) {
            textInputRef.current?.focus();
        }
    }, [adding]);

    useEffect(() => {
        setInputError(null);
    }, [textInput, adding]);

    function onAddClick() {
        setAdding(true);
    }

    function onTextInputKeyDown(e: React.KeyboardEvent<HTMLInputElement>) {
        if (e.key === 'Enter') {
            let callbackCalled = false;
            const errorCallback = (message: string) => {
                setInputError(message);
                callbackCalled = true;
            }
            props.onAdd?.(textInput, errorCallback);
            if (!callbackCalled) {
                setTextInput("");
                setAdding(false);
            }
        } else if (e.key === 'Escape') {
            setAdding(false);
            setTextInput("");
        }
    }

    return (
        <div className={s.root}>
            { props.label && <label className={s.label}>{ props.label }</label> }
            <div className={s.items}>
                { props.items.map((value, index) => (
                    <div className={s.item} key={index}>
                        <span className={s.value}>{ value }</span>
                        <span className={s.controls}>
                            <Button
                                className={s.deleteButton}
                                onClick={() => props.onDelete?.(value, index)}
                                noStyle
                            >
                                <DeleteIcon className={s.icon} />
                            </Button>
                        </span>
                    </div>
                )) }
            </div>
            { !maxItemsReached && !adding && (
                <Button className={s.addButton} onClick={onAddClick} disabled={props.disabled}>
                    <AddIcon className={s.addIcon}/>
                    Добавить
                </Button>
            ) }
            { adding && (
                <TextInput
                    value={textInput}
                    className={s.textInput}
                    onChange={setTextInput}
                    onKeyDown={onTextInputKeyDown}
                    error={inputError}
                    disabled={props.disabled}
                    ref={textInputRef}
                />
            ) }
        </div>
    )
}
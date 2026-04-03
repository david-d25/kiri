import {useState} from "react";
import s from "./MemoryCreateModal.module.scss";
import TextArea from "@/components/TextArea/TextArea";
import TextInput from "@/components/TextInput/TextInput";
import Button from "@/components/Button/Button";
import {ModalComponentProps} from "@/components/Modal/ModalProvider";

export type MemoryCreateResult = {
    value: string;
    keys: string[];
};

type Props = ModalComponentProps<MemoryCreateResult>;

export default function MemoryCreateModal({ onSubmit, onCancel }: Props) {
    const [value, setValue] = useState("");
    const [keysText, setKeysText] = useState("");

    function handleCreate() {
        if (!value.trim()) return;
        const keys = keysText
            .split(",")
            .map(k => k.trim())
            .filter(k => k.length > 0);
        onSubmit({ value: value.trim(), keys });
    }

    return (
        <div className={s.root}>
            <TextArea
                value={value}
                onChange={setValue}
                label="Memory value"
                placeholder="The fact or information to remember..."
                rows={4}
            />
            <TextInput
                value={keysText}
                onChange={setKeysText}
                label="Keys (comma-separated)"
                placeholder="name, date, topic, ..."
            />
            <div className={s.actions}>
                <Button onClick={onCancel}>Cancel</Button>
                <Button onClick={handleCreate} colorAccent="primary" disabled={!value.trim()}>
                    Create
                </Button>
            </div>
        </div>
    );
}

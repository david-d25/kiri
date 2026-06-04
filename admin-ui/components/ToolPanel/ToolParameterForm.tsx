import {useEffect, useRef, useState} from "react";
import TextInput from "@/components/TextInput/TextInput";
import NumberInput from "@/components/NumberInput/NumberInput";
import Checkbox from "@/components/Checkbox/Checkbox";
import Dropdown from "@/components/Dropdown/Dropdown";
import TextArea from "@/components/TextArea/TextArea";
import {ToolParameterValueDto} from "@/lib/api/types/ToolDto";
import {ToolInputDto} from "@/lib/api/types/FrameDto";
import s from "./ToolPanel.module.scss";

type Props = {
    schema: ToolParameterValueDto;
    value: ToolInputDto;
    onChange: (value: ToolInputDto) => void;
};

export default function ToolParameterForm({schema, value, onChange}: Props) {
    if (schema.type !== "object") {
        return <ParameterField schema={schema} value={value} onChange={onChange}/>;
    }

    const objectValue = value.type === "object" ? value : {type: "object" as const, items: {}};

    return (
        <div className={s.parameterGroup}>
            {Object.entries(schema.properties).map(([key, propSchema]) => {
                const isRequired = schema.required.includes(key);
                return (
                    <div key={key} className={s.parameterField}>
                        <label className={s.paramLabel}>
                            {key}{isRequired ? " *" : ""}
                            {propSchema.description && (
                                <span className={s.paramDescription}> — {propSchema.description}</span>
                            )}
                        </label>
                        <ParameterField
                            schema={propSchema}
                            value={objectValue.items[key]}
                            onChange={(v) => {
                                if (!isRequired && isEmptyInput(v, propSchema)) {
                                    const {[key]: _, ...rest} = objectValue.items;
                                    onChange({type: "object", items: rest});
                                } else {
                                    onChange({type: "object", items: {...objectValue.items, [key]: v}});
                                }
                            }}
                        />
                    </div>
                );
            })}
        </div>
    );
}

function ParameterField({schema, value, onChange}: {
    schema: ToolParameterValueDto;
    value: ToolInputDto | undefined;
    onChange: (v: ToolInputDto) => void;
}) {
    switch (schema.type) {
        case "string":
            if (schema.enum && schema.enum.length > 0) {
                const textVal = value?.type === "text" ? value.text : "";
                return (
                    <Dropdown
                        options={schema.enum.map(e => ({label: e, value: e}))}
                        selectedValue={textVal}
                        onChange={(v) => onChange({type: "text", text: v})}
                    />
                );
            }
            return (
                <TextInput
                    value={value?.type === "text" ? value.text : ""}
                    onChange={(v) => onChange({type: "text", text: v})}
                />
            );
        case "number":
            return (
                <NumberInput
                    value={value?.type === "number" ? value.number : null}
                    onChange={(v) => onChange({type: "number", number: v})}
                />
            );
        case "boolean":
            return (
                <Checkbox
                    label=""
                    checked={value?.type === "boolean" ? value.boolean : false}
                    onChange={(v) => onChange({type: "boolean", boolean: v})}
                />
            );
        case "array":
            return <ArrayField value={value} onChange={onChange}/>;
        case "object":
            return (
                <ToolParameterForm
                    schema={schema}
                    value={value ?? {type: "object", items: {}}}
                    onChange={onChange}
                />
            );
    }
}

function ArrayField({value, onChange}: {
    value: ToolInputDto | undefined;
    onChange: (v: ToolInputDto) => void;
}) {
    const externalJson = JSON.stringify(toolInputToJson(value ?? {type: "array", items: []}));
    const [text, setText] = useState(() =>
        JSON.stringify(toolInputToJson(value ?? {type: "array", items: []}), null, 2)
    );
    const [error, setError] = useState<string | null>(null);
    const lastEmittedRef = useRef(externalJson);

    useEffect(() => {
        if (externalJson !== lastEmittedRef.current) {
            setText(JSON.stringify(JSON.parse(externalJson), null, 2));
            setError(null);
            lastEmittedRef.current = externalJson;
        }
    }, [externalJson]);

    return (
        <TextArea
            value={text}
            onChange={(v) => {
                setText(v);
                try {
                    const parsed = JSON.parse(v);
                    const dto = jsonToToolInput(parsed);
                    setError(null);
                    lastEmittedRef.current = JSON.stringify(parsed);
                    onChange(dto);
                } catch (e) {
                    setError(e instanceof Error ? e.message : "Invalid JSON");
                }
            }}
            rows={3}
            placeholder='["item1", "item2"]'
            error={error}
        />
    );
}

function toolInputToJson(v: ToolInputDto): unknown {
    switch (v.type) {
        case "text": return v.text;
        case "number": return v.number;
        case "boolean": return v.boolean;
        case "array": return v.items.map(toolInputToJson);
        case "object": {
            const out: { [k: string]: unknown } = {};
            for (const [k, item] of Object.entries(v.items)) out[k] = toolInputToJson(item);
            return out;
        }
    }
}

function isEmptyInput(v: ToolInputDto, schema: ToolParameterValueDto): boolean {
    switch (schema.type) {
        case "string": return v.type === "text" && v.text === "";
        case "number": return v.type === "number" && v.number === null;
        default: return false;
    }
}

function jsonToToolInput(val: unknown): ToolInputDto {
    if (typeof val === "string") return {type: "text", text: val};
    if (typeof val === "number") return {type: "number", number: val};
    if (typeof val === "boolean") return {type: "boolean", boolean: val};
    if (Array.isArray(val)) return {type: "array", items: val.map(jsonToToolInput)};
    if (typeof val === "object" && val !== null) {
        const items: { [k: string]: ToolInputDto } = {};
        for (const [k, v] of Object.entries(val)) items[k] = jsonToToolInput(v);
        return {type: "object", items};
    }
    return {type: "text", text: String(val)};
}

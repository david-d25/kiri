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
                                onChange({type: "object", items: {...objectValue.items, [key]: v}});
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
                    onChange={(v) => onChange({type: "number", number: v ?? 0})}
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
        case "array": {
            const jsonStr = value ? JSON.stringify(value, null, 2) : "[]";
            return (
                <TextArea
                    value={jsonStr}
                    onChange={(v) => {
                        try {
                            onChange(jsonToToolInput(JSON.parse(v)));
                        } catch { /* ignore parse errors while typing */ }
                    }}
                    rows={3}
                    placeholder='["item1", "item2"]'
                />
            );
        }
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

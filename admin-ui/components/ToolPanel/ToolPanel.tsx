import {useState} from "react";
import s from "./ToolPanel.module.scss";
import Panel from "@/components/Panel/Panel";
import Dropdown from "@/components/Dropdown/Dropdown";
import Button from "@/components/Button/Button";
import {useTools, useExecuteTool} from "@/hooks/tools";
import {ToolInputDto} from "@/lib/api/types/FrameDto";
import ToolParameterForm from "./ToolParameterForm";

type Props = { className?: string };

export default function ToolPanel({className}: Props) {
    const {data: tools, isLoading} = useTools();
    const executeMutation = useExecuteTool();
    const [selectedTool, setSelectedTool] = useState<string | null>(null);
    const [inputState, setInputState] = useState<ToolInputDto>({type: "object", items: {}});

    const tool = tools?.find(t => t.fullName === selectedTool);
    const options = (tools ?? []).map(t => ({label: t.fullName, value: t.fullName}));

    const handleSelectTool = (fullName: string) => {
        setSelectedTool(fullName);
        setInputState({type: "object", items: {}});
    };

    const handleExecute = () => {
        if (!selectedTool) return;
        executeMutation.mutate({toolName: selectedTool, input: inputState});
    };

    return (
        <Panel className={className}>
            <div className={s.header}>Tools</div>
            {isLoading && <div className={s.muted}>Loading...</div>}
            {!isLoading && options.length === 0 && (
                <div className={s.muted}>No tools available. Run at least one tick first.</div>
            )}
            {options.length > 0 && (
                <Dropdown
                    options={options}
                    selectedValue={selectedTool ?? ""}
                    placeholder="Select a tool..."
                    onChange={handleSelectTool}
                />
            )}
            {tool && (
                <>
                    {tool.description && <div className={s.description}>{tool.description}</div>}
                    <ToolParameterForm
                        schema={tool.parameters}
                        value={inputState}
                        onChange={setInputState}
                    />
                    <Button
                        onClick={handleExecute}
                        disabled={executeMutation.isPending}
                        colorAccent="primary"
                    >
                        {executeMutation.isPending ? "Executing..." : "Execute"}
                    </Button>
                    {executeMutation.isError && (
                        <div className={s.error}>
                            Error: {executeMutation.error?.message}
                        </div>
                    )}
                </>
            )}
        </Panel>
    );
}

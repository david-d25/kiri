import s from "./AgentSettings.module.scss";
import {useEffect, useState} from "react";
import InfoPanel from "@/components/InfoPanel/InfoPanel";
import Button from "@/components/Button/Button";
import Container from "../Container/Container";
import {useSettingsFormState} from "@/hooks/useSettingsFormState";
import {ChatCompletionModelDto} from "@/lib/api/types/ChatCompletionModelDto";
import LoadingOverlay from "@/components/LoadingOverlay/LoadingOverlay";
import ChatCompletionModelSelector from "@/components/ChatCompletionModelSelector/ChatCompletionModelSelector";
import FormLabel from "@/components/FormLabel/FormLabel";
import LlmMaxTokensInput from "@/components/AgentSettings/common/LlmMaxTokensInput";
import ReasoningSettingsCollapsibleInput from "@/components/AgentSettings/common/ReasoningSettingsCollapsibleInput";
import Checkbox from "@/components/Checkbox/Checkbox";
import InstructionsInput from "@/components/AgentSettings/common/InstructionsInput";


export default function AgentSettings() {
    return (
        <div className={s.root}>
            <Container>
                <DiscussionChatSettings/>
            </Container>
        </div>
    );
}

function DiscussionChatSettings() {
    const {
        settings,
        loading,
        anyError,
        anyChanged,
        clearErrors,
        save,
        reset
    } = useSettingsFormState({
        instructions:       { key: "agent.engine.content.instructions", defaultValue: "" },
        modelHandle:        { key: "agent.engine.modelHandle", defaultValue: null as string | null },
        maxOutputTokens:    { key: "agent.engine.llm.maxOutputTokens", defaultValue: 1024 },
        reasoningEnabled:   { key: "agent.engine.llm.reasoning.enabled", defaultValue: false },
        reasoningMaxTokens: { key: "agent.engine.llm.reasoning.maxTokens", defaultValue: 2048 },
        reasoningEffort:    { key: "agent.engine.llm.reasoning.effort", defaultValue: null as string | null },
        webSearchEnabled:   { key: "agent.engine.llm.tool.external.webSearch.enabled", defaultValue: false },
    });

    // Model features
    const [ modelFeatures, setModelFeatures ] = useState<ChatCompletionModelDto.Features | null>(null);
    const [ modelReasoningType, setModelReasoningType ] = useState<ChatCompletionModelDto.ReasoningType | null>(null);

    useEffect(resetModelHandleError, [settings.modelHandle.value]);

    function resetModelHandleError() {
        settings.modelHandle.setValidationError(null);
    }

    function onSave() {
        save()
    }

    function onCancel() {
        if (loading) {
            return;
        }
        reset();
        clearErrors();
    }

    function onChatCompletionModelSelectorChange(model: ChatCompletionModelDto | null) {
        settings.modelHandle.setValue(model ? model.handle : null);
        setModelFeatures(model ? model.features : null);
        setModelReasoningType(model ? model.reasoningType : null);
    }

    return (
        <div className={s.column}>
            { anyError && (
                <InfoPanel type={'error'}>{anyError.message}</InfoPanel>
            ) }
            <LoadingOverlay loading={loading}>
                <div className={s.column}>
                    <ChatCompletionModelSelector
                        className={s.thin}
                        modelHandle={settings.modelHandle.value}
                        onChange={onChatCompletionModelSelectorChange}
                        error={settings.modelHandle.validationError}
                        label={<FormLabel changed={settings.modelHandle.isChanged}>LLM</FormLabel>}
                    />
                    <LlmMaxTokensInput
                        setting={settings.maxOutputTokens}
                    />
                    <ReasoningSettingsCollapsibleInput
                        reasoningEnabledSetting={settings.reasoningEnabled}
                        reasoningMaxTokensSetting={settings.reasoningMaxTokens}
                        reasoningEffortSetting={settings.reasoningEffort}
                        modelReasoningType={modelReasoningType}
                        modelFeatures={modelFeatures}
                        disabled={loading || !!anyError}
                    />
                    <Checkbox
                        className={s.thin}
                        disabled={loading || !!anyError}
                        checked={settings.webSearchEnabled.value}
                        onChange={settings.webSearchEnabled.setValue}
                        label={
                            <FormLabel changed={settings.webSearchEnabled.isChanged}>
                                Enable web search
                            </FormLabel>
                        }
                    />
                    <InstructionsInput
                        setting={settings.instructions}
                        disabled={loading || !!anyError}
                    />
                </div>
            </LoadingOverlay>
            <div className={s.row}>
                <Button
                    colorAccent={'primary'}
                    disabled={!anyChanged || loading}
                    onClick={onSave}
                >
                    Save
                </Button>
                { anyChanged && (
                    <Button onClick={onCancel} disabled={loading}>Cancel</Button>
                ) }
            </div>
        </div>
    )
}
import s from "./AgentSettings.module.scss";
import {useGetSettingsByKeys, useUpdateSettings} from "@/hooks/settings";
import {useGetChatCompletionModels} from "@/hooks/chatCompletion";
import {SettingDto} from "@/lib/api/types/SettingDto";
import {useEffect, useState} from "react";
import {useDebouncedCallback} from "@/lib/useDebouncedCallback";
import {Tiktoken} from "js-tiktoken/lite";
// @ts-ignore I don't know why my IDE complains about this import, but it works
import o200k_base from "js-tiktoken/ranks/o200k_base";
import InfoPanel from "@/components/InfoPanel/InfoPanel";
import MultiSelect from "@/components/MultiSelect/MultiSelect";
import Link from "next/link";
import TextArea from "@/components/TextArea/TextArea";
import Button from "@/components/Button/Button";
import Container from "../Container/Container";

const tokenEncoding = new Tiktoken(o200k_base);

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
    const messageStatsPerformanceWarningThreshold = 1000000;

    const Keys = {
        ASSISTANT_INSTRUCTIONS: 'agent.engine.content.instructions',
        MODEL_HANDLE: 'agent.engine.modelHandle',
    };

    const settingsRequest = useGetSettingsByKeys([
        Keys.ASSISTANT_INSTRUCTIONS,
        Keys.MODEL_HANDLE,
    ]);
    const updateRequest = useUpdateSettings();
    const modelsRequest = useGetChatCompletionModels();

    const settingByKey = settingsRequest.data?.reduce<Record<string, SettingDto>>((acc, setting) => {
        acc[setting.key] = setting;
        return acc;
    }, {}) || {};
    const modelOptions = modelsRequest.data?.map(model => model.handle) || [];

    // Original values
    const assistantInstructionsSettingValue = settingByKey[Keys.ASSISTANT_INSTRUCTIONS]?.value;
    const modelHandleSettingValue = settingByKey[Keys.MODEL_HANDLE]?.value;

    // Input states
    const [ assistantInstructions, setAssistantInstructions ] = useState('');
    const [ modelHandle, setModelHandle ] = useState<string | null>(null);

    // Input errors
    const [ modelHandleError, setModelHandleError ] = useState<string | null>(null);

    // Stats
    const [ symbolCount, setSymbolCount ] = useState(0);
    const [ o200KBaseTokenCount, setO200KBaseTokenCount ] = useState(0);

    // Change detection
    const assistantInstructionsChanged = (assistantInstructionsSettingValue || '') !== assistantInstructions;
    const modelHandleChanged = modelHandleSettingValue !== modelHandle;
    const anythingChanged = assistantInstructionsChanged || modelHandleChanged;

    const loading = settingsRequest.isLoading || updateRequest.isPending || modelsRequest.isLoading;

    const recalculateStats = useDebouncedCallback(recalculateStatsSync, 300);

    const anyError = updateRequest.error || settingsRequest.error || modelsRequest.error;

    useEffect(recalculateStats, [assistantInstructions]);

    useEffect(() => {
        if (settingsRequest.data) {
            setAssistantInstructions(assistantInstructionsSettingValue || '');
            setModelHandle(modelHandleSettingValue);
        }
    }, [settingsRequest.data]);

    useEffect(() => {
        setModelHandleError(null);
    }, [modelHandle]);

    function recalculateStatsSync() {
        setSymbolCount(assistantInstructions.length);
        if (assistantInstructions.length > messageStatsPerformanceWarningThreshold) {
            setO200KBaseTokenCount(-1);
        } else {
            setO200KBaseTokenCount(tokenEncoding.encode(assistantInstructions).length);
        }
    }

    function onSave() {
        if (loading) {
            return;
        }
        updateRequest.mutate({
            updates: {
                [Keys.ASSISTANT_INSTRUCTIONS]: {
                    value: assistantInstructions,
                },
                [Keys.MODEL_HANDLE]: {
                    value: modelHandle,
                },
            }
        });
    }

    function onCancel() {
        if (loading) {
            return;
        }
        setAssistantInstructions(assistantInstructionsSettingValue || '');
        setModelHandle(modelHandleSettingValue);
    }

    return (
        <div className={s.column}>
            { anyError && (
                <InfoPanel type={'error'}>{anyError.message}</InfoPanel>
            ) }
            <MultiSelect
                items={modelOptions}
                selectedItems={modelHandle ? [modelHandle] : []}
                onSelect={it => setModelHandle(it)}
                onDeselect={() => setModelHandle(null)}
                itemToLabel={it => it}
                itemToIdentityKey={it => it}
                maxItems={1}
                disabled={loading || !!anyError}
                error={modelHandleError}
                label={'LLM' + (modelHandleChanged ? '*' : '')}
                className={s.modelHandleInput}
            />
            { modelsRequest.data && modelOptions.length == 0 && (
                <InfoPanel minimalistic borderless type={'warning'}>
                    No models available. Set up <Link href={'/integrations'}>integrations</Link>.
                </InfoPanel>
            ) }
            <TextArea
                value={assistantInstructions}
                onChange={setAssistantInstructions}
                disabled={loading || !!anyError}
                label={'Instructions' + (assistantInstructionsChanged ? '*' : '')}
                rows={30}
            />
            <div className={s.stats}>
                <span>{symbolCount} symbols</span>
                { assistantInstructions.length <= messageStatsPerformanceWarningThreshold && (
                    <>
                        <span>, </span>
                        <span>
                            {o200KBaseTokenCount} tokens (o200k_base)
                        </span>
                    </>
                ) }
            </div>
            { assistantInstructions.length > messageStatsPerformanceWarningThreshold && (
                <div className={s.row}>
                    <InfoPanel type={'warning'} borderless minimalistic>
                        Token count disabled for performance
                    </InfoPanel>
                </div>
            ) }
            <div className={s.row}>
                <Button
                    colorAccent={'primary'}
                    disabled={!anythingChanged || loading}
                    onClick={onSave}
                >
                    Save
                </Button>
                { anythingChanged && (
                    <Button onClick={onCancel} disabled={loading}>Cancel</Button>
                ) }
            </div>
        </div>
    )
}
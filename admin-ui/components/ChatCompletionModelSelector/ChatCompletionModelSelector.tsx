import s from "./ChatCompletionModelSelector.module.scss";

import {useGetChatCompletionModels} from "@/hooks/chatCompletion";
import MultiSelect from "@/components/MultiSelect/MultiSelect";
import InfoPanel from "@/components/InfoPanel/InfoPanel";
import Link from "next/link";
import {ChatCompletionModelDto} from "@/lib/api/types/ChatCompletionModelDto";
import React, {useEffect} from "react";
import GlobeIcon from "@/icons/globe.svg";
import {classnames} from "@/lib/classnames";

export type ChatCompletionModelSelectorProps = {
    modelHandle: string | null;
    onChange?: (handle: ChatCompletionModelDto | null) => void;
    error?: string | null;
    label?: React.ReactNode;
    className?: string;
    muted?: boolean;
}

export default function (props: ChatCompletionModelSelectorProps) {
    const modelsRequest = useGetChatCompletionModels();
    const modelOptions = modelsRequest.data || [];

    const loading = modelsRequest.isLoading;
    const error = modelsRequest.error;

    const selectedValues = props.modelHandle ? [asModel(props.modelHandle)] : [];

    function asModel(handle: string): ChatCompletionModelDto {
        const found = modelsRequest.data?.find(it => it.handle == handle);
        if (found) {
            return found;
        } else {
            return {
                handle: handle,
                reasoningType: "NONE",
                features: {
                    webSearch: false,
                    reasoningEffort: false,
                    reasoningMaxTokens: false,
                }
            }
        }
    }

    function asHandle(it: ChatCompletionModelDto): string {
        return it.handle;
    }

    function valueToItem(value: ChatCompletionModelDto): React.ReactNode {
        return (
            <span className={s.item}>
                <span className={s.value}>{value.handle}</span>
                { value.features.webSearch && (
                    <GlobeIcon className={s.icon}/>
                ) }
                { value.reasoningType !== "NONE" && (
                    <span className={s.reasoningIcon}>R</span>
                ) }
            </span>
        );
    }

    useEffect(() => {
        if (modelsRequest.data && props.modelHandle) {
            props.onChange?.(asModel(props.modelHandle))
        }
    }, [modelsRequest.data, props.modelHandle]);

    return (
        <div className={classnames(s.root, props.className)}>
            <MultiSelect
                possibleValues={modelOptions}
                selectedValues={selectedValues}
                onSelect={it => props.onChange?.(it)}
                onDeselect={() => props.onChange?.(null)}
                valueToSearchString={asHandle}
                valueToIdentityKey={asHandle}
                valueToItem={valueToItem}
                maxItems={1}
                disabled={loading}
                muted={props.muted}
                error={error?.message || props.error}
                label={props.label}
                className={s.modelHandleInput}
            />
            { modelsRequest.data && modelOptions.length == 0 && (
                <InfoPanel minimalistic borderless type={'warning'} className={s.infoPanel}>
                    No models available. Set up <Link href={'/integrations'}>integrations</Link> with
                    AI providers
                </InfoPanel>
            ) }
        </div>
    )
}
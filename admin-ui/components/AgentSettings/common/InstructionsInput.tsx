import {FormSettingDescriptor} from "@/hooks/useSettingsFormState";
import TextArea from "@/components/TextArea/TextArea";
// @ts-ignore I don't know why my IDE complains about this import, but it works
import o200k_base from "js-tiktoken/ranks/o200k_base";
import {Tiktoken} from "js-tiktoken/lite";
import {useEffect, useState} from "react";
import {useDebouncedCallback} from "@/lib/useDebouncedCallback";
import s from "@/components/AgentSettings/AgentSettings.module.scss";
import InfoPanel from "@/components/InfoPanel/InfoPanel";
import FormLabel from "@/components/FormLabel/FormLabel";

const tokenEncoding = new Tiktoken(o200k_base);

type Props = {
    setting: FormSettingDescriptor<string>;
    disabled?: boolean;
    muted?: boolean;
}

export default function InstructionsInput(props: Props) {
    const messageStatsPerformanceWarningThreshold = 1000000;
    const { setting, disabled } = props;

    const [ symbolCount, setSymbolCount ] = useState(0);
    const [ o200KBaseTokenCount, setO200KBaseTokenCount ] = useState(0);

    const recalculateStats = useDebouncedCallback(recalculateStatsSync, 300);

    useEffect(recalculateStats, [setting.value]);

    function recalculateStatsSync() {
        const instructions = setting.value;
        setSymbolCount(instructions.length);
        if (instructions.length > messageStatsPerformanceWarningThreshold) {
            setO200KBaseTokenCount(-1);
        } else {
            setO200KBaseTokenCount(tokenEncoding.encode(instructions).length);
        }
    }

    return (
        <div className={s.column}>
            <TextArea
                value={setting.value}
                onChange={setting.setValue}
                disabled={disabled}
                muted={props.muted}
                label={ <FormLabel changed={setting.isChanged}>Instructions</FormLabel> }
                autocorrect={'off'}
                autocapitalize={'off'}
                spellcheck={false}
                rows={24}
            />
            <div className={s.stats}>
                <span>{symbolCount} symbols</span>
                { setting.value.length <= messageStatsPerformanceWarningThreshold && (
                    <>
                        <span>, </span>
                        <span>{ o200KBaseTokenCount } tokens (o200k_base)</span>
                    </>
                ) }
            </div>
            { setting.value.length > messageStatsPerformanceWarningThreshold && (
                <div className={s.row}>
                    <InfoPanel type={'warning'} borderless minimalistic>
                        Token count disabled for performance
                    </InfoPanel>
                </div>
            ) }
        </div>
    )
}
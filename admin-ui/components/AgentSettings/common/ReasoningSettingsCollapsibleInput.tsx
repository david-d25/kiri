import s from "@/components/AgentSettings/AgentSettings.module.scss";
import {classnames} from "@/lib/classnames";
import Checkbox from "@/components/Checkbox/Checkbox";
import NumberInput from "@/components/NumberInput/NumberInput";
import Dropdown from "@/components/Dropdown/Dropdown";
import Collapsible from "@/components/Collapsible/Collapsible";
import {FormSettingDescriptor} from "@/hooks/useSettingsFormState";
import {useEffect, useState} from "react";
import {ChatCompletionModelDto} from "@/lib/api/types/ChatCompletionModelDto";
import FormLabel from "@/components/FormLabel/FormLabel";

const reasoningEffortOptions = [
    { value: null, label: 'Auto' },
    { value: 'low', label: 'Low' },
    { value: 'medium', label: 'Medium' },
    { value: 'high', label: 'High' },
]

type Props = {
    reasoningEnabledSetting: FormSettingDescriptor<boolean>;
    reasoningMaxTokensSetting: FormSettingDescriptor<number>;
    reasoningEffortSetting: FormSettingDescriptor<string | null>;
    modelReasoningType: ChatCompletionModelDto.ReasoningType | null;
    modelFeatures: ChatCompletionModelDto.Features | null;
    disabled?: boolean;
    muted?: boolean;
};

export default function ReasoningSettingsCollapsibleInput(props: Props) {
    const {
        reasoningEnabledSetting,
        reasoningMaxTokensSetting,
        reasoningEffortSetting,
        modelReasoningType,
        modelFeatures,
        disabled,
        muted,
    } = props;

    const [ reasoningSectionOpened, setReasoningSectionOpened ] = useState(false);

    useEffect(() => {
        if (modelReasoningType == "REQUIRED") {
            reasoningEnabledSetting.setValue(true);
        }
    }, [modelReasoningType, reasoningEnabledSetting.value]);

    return (
        <Collapsible
            className={s.thin}
            opened={reasoningSectionOpened}
            onOpenChange={setReasoningSectionOpened}
            header={
                <span className={classnames(s.thin, s.collapsibleHeader)}>
                    <Checkbox
                        label={''}
                        disabled={disabled || modelReasoningType == "REQUIRED"}
                        checked={reasoningEnabledSetting.value}
                        onChange={reasoningEnabledSetting.setValue}
                    />
                    <FormLabel changed={reasoningEnabledSetting.isChanged}>
                        <span className={classnames({ [s.muted]: muted })}>
                            Reasoning
                        </span>
                    </FormLabel>
                </span>
            }
        >
            <div className={s.column}>
                {modelFeatures?.reasoningMaxTokens && (
                    <NumberInput
                        className={s.thin}
                        value={reasoningMaxTokensSetting.value}
                        minValue={0}
                        maxValue={65536}
                        disabled={disabled}
                        onChange={value => reasoningMaxTokensSetting.setValue(value || 0)}
                        label={
                            <FormLabel changed={reasoningMaxTokensSetting.isChanged}>
                                Reasoning tokens budget
                            </FormLabel>
                        }
                        error={reasoningMaxTokensSetting.validationError}
                        step={1}
                    />
                )}
                {modelFeatures?.reasoningEffort && (
                    <Dropdown
                        className={s.thin}
                        options={reasoningEffortOptions}
                        selectedValue={reasoningEffortSetting.value}
                        onChange={reasoningEffortSetting.setValue}
                        disabled={disabled}
                        label={
                            <FormLabel changed={reasoningEffortSetting.isChanged}>
                                Reasoning effort
                            </FormLabel>
                        }
                    />
                )}
            </div>
        </Collapsible>
    );
}
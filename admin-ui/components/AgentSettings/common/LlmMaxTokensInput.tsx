import {FormSettingDescriptor} from "@/hooks/useSettingsFormState";
import s from "@/components/AgentSettings/AgentSettings.module.scss";
import NumberInput from "@/components/NumberInput/NumberInput";
import FormLabel from "@/components/FormLabel/FormLabel";

type Props = {
    setting: FormSettingDescriptor<number>;
    muted?: boolean;
}

export default function LlmMaxTokensInput(props: Props) {
    return (
        <NumberInput
            className={s.thin}
            value={props.setting.value}
            minValue={64}
            maxValue={65536}
            onChange={value => props.setting.setValue(value || 0)}
            error={props.setting.validationError}
            label={ <FormLabel changed={props.setting.isChanged}>Max output tokens</FormLabel> }
            muted={props.muted}
            step={1}
        />
    )
}
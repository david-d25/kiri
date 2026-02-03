import s from "./SettingToggle.module.scss";
import Toggle from "@/components/Toggle/Toggle";
import {useGetSetting, useUpdateSetting} from "@/hooks/settings";
import React, {useEffect} from "react";
import InfoPanel from "@/components/InfoPanel/InfoPanel";
import LoadingDotsIcon from "@/icons/loading-dots.svg";

type SettingToggleProps = {
    settingKey: string;
    label?: React.ReactNode;
    disabled?: boolean;
    onChange?: (checked: boolean) => void;
}

export default function SettingToggle(props: SettingToggleProps) {
    const request = useGetSetting(props.settingKey);
    const updateRequest = useUpdateSetting(props.settingKey);

    useEffect(() => {
        if (updateRequest.data) {
            props.onChange?.(updateRequest.data.value == 'true')
        }
    }, [updateRequest.data]);

    function onChange(checked: boolean) {
        updateRequest.mutate({
            value: checked.toString()
        })
    }

    const loading = request.isLoading || updateRequest.isPending;
    const anyError = request.error || updateRequest.error;
    const isChecked = (request.data?.value == 'true');
    const disabled = props.disabled || loading || request.isError;
    const toggleLabel = loading
        ? (<span>{props.label} <LoadingDotsIcon loading={loading} className={s.loadingIcon} /></span>)
        : props.label;
    return (
        <>
            <Toggle checked={isChecked} onChange={onChange} label={toggleLabel} disabled={disabled} />
            { anyError && (
                <InfoPanel type={'error'}>{anyError.message}</InfoPanel>
            ) }
        </>
    );
}
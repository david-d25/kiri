import {useEffect, useState} from "react";
import {SettingDto} from "@/lib/api/types/SettingDto";
import {useGetSettingsByKeys, useUpdateSettings} from "@/hooks/settings";
import {SettingUpdateRequest} from "@/lib/api/types/SettingUpdateRequest";

export type KeyAndDefault<T> = {
    key: string;
    defaultValue: T;
}

export type FormSettingDescriptor<T> = {
    key: string;
    defaultValue: T;
    originalValue?: SettingDto;
    value: T;
    setValue: (v: T) => void;
    isChanged: boolean;
    validationError: string | null;
    setValidationError: (e: string | null) => void;
}

export function useSettingsFormState<K extends Record<string, KeyAndDefault<any>>>(keys: K) {
    const [ originalSettings, setOriginalSettings ] = useState<Record<string, SettingDto>>({});

    type SettingsMap = { [P in keyof K]: FormSettingDescriptor<K[P]['defaultValue']> };
    const settings = {} as SettingsMap;

    for (const k of Object.keys(keys) as Array<keyof K>) {
        const settingKey = keys[k].key;
        const defaultValue = keys[k].defaultValue;
        settings[k] = createDescriptor(settingKey, defaultValue) as unknown as SettingsMap[typeof k];
    }

    const settingsRequest = useGetSettingsByKeys(Object.values(settings).map(it => it.key));
    const updateRequest = useUpdateSettings();

    useEffect(reset, [settingsRequest.data]);

    const loading = settingsRequest.isLoading || updateRequest.isPending;
    const anyError = updateRequest.error || settingsRequest.error;
    const anyChanged = Object.values(settings).some(s => s.isChanged);

    return {
        settings,
        originalSettings,
        setOriginalSettings,
        anyChanged,
        anyError,
        loading,
        reset,
        save,
        clearErrors,
    };

    function clearErrors() {
        for (const descriptor of Object.values(settings)) {
            descriptor.setValidationError(null);
        }
    }

    function reset() {
        if (!settingsRequest.data) {
            return;
        }
        const newOriginalSettings: { [key: string]: SettingDto } = {};
        for (const setting of settingsRequest.data) {
            newOriginalSettings[setting.key] = setting;
        }
        setOriginalSettings(newOriginalSettings);

        for (const descriptor of Object.values(settings)) {
            const defaultValue = descriptor.defaultValue;
            const originalValue = newOriginalSettings[descriptor.key]?.value;
            if (typeof defaultValue === 'boolean') {
                descriptor.setValue((originalValue || defaultValue.toString()) === 'true');
            } else if (typeof defaultValue === 'number') {
                descriptor.setValue(originalValue == null ? defaultValue : +originalValue);
            } else if (defaultValue == null) {
                descriptor.setValue(originalValue || null);
            } else {
                descriptor.setValue(originalValue || defaultValue);
            }
        }
    }

    function save() {
        if (loading) {
            return;
        }
        const updates: { [key: string]: SettingUpdateRequest } = {};
        for (const descriptor of Object.values(settings)) {
            if (descriptor.isChanged) {
                updates[descriptor.key] = {
                    value: descriptor.value?.toString() || null,
                }
            }
        }
        updateRequest.mutate({ updates });
    }

    function createDescriptor<T>(key: string, defaultValue: T) {
        const [ value, setValue ] = useState<T>(defaultValue);
        const [ validationError, setValidationError ] = useState<string | null>(null);
        let isChanged = true;
        if (typeof value === 'boolean') {
            const originalValue = originalSettings[key]?.value;
            isChanged = value != (originalValue === 'true');
        }
        if (typeof value === 'number') {
            const originalValue = originalSettings[key]?.value;
            isChanged = value != (originalValue == null ? defaultValue : +originalValue);
        }
        if (typeof value === 'string' || value === null) {
            const originalValue = originalSettings[key]?.value;
            isChanged = value != (originalValue || defaultValue);
        }
        return {
            key: key,
            defaultValue: defaultValue,
            originalValue: originalSettings[key],
            value,
            setValue,
            isChanged,
            validationError,
            setValidationError,
        }
    }
}
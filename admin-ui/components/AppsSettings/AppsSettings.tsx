import s from "./AppsSettings.module.scss";
import Container from "@/components/Container/Container";
import {useSettingsFormState} from "@/hooks/useSettingsFormState";
import LoadingOverlay from "@/components/LoadingOverlay/LoadingOverlay";
import InfoPanel from "@/components/InfoPanel/InfoPanel";
import Checkbox from "@/components/Checkbox/Checkbox";
import FormLabel from "@/components/FormLabel/FormLabel";
import Button from "@/components/Button/Button";

export default function AppsSettings() {
    const {
        settings,
        loading,
        anyError,
        anyChanged,
        clearErrors,
        save,
        reset
    } = useSettingsFormState({
        tgAutoSwitchOnWake: { key: "apps.telegram.autoSwitchOnWake", defaultValue: true },
    });

    function onSave() {
        save();
    }

    function onCancel() {
        if (loading) return;
        reset();
        clearErrors();
    }

    return (
        <div className={s.root}>
            <Container>
                <div className={s.column}>
                    {anyError && (
                        <InfoPanel type={'error'}>{anyError.message}</InfoPanel>
                    )}
                    <h2>Telegram</h2>
                    <LoadingOverlay loading={loading}>
                        <div className={s.column}>
                            <Checkbox
                                className={s.thin}
                                disabled={loading || !!anyError}
                                checked={settings.tgAutoSwitchOnWake.value}
                                onChange={settings.tgAutoSwitchOnWake.setValue}
                                label={
                                    <FormLabel changed={settings.tgAutoSwitchOnWake.isChanged}>
                                        Auto-switch Telegram chat on wake
                                    </FormLabel>
                                }
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
                        {anyChanged && (
                            <Button onClick={onCancel} disabled={loading}>Cancel</Button>
                        )}
                    </div>
                </div>
            </Container>
        </div>
    );
}
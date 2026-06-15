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
        tgRespondToKiriPrefix: { key: "apps.telegram.respondToKiriPrefix", defaultValue: false },
        tgReactionTool: { key: "apps.telegram.tools.reaction.enabled", defaultValue: false },
        tgEditMessageTool: { key: "apps.telegram.tools.editMessage.enabled", defaultValue: false },
        tgDeleteMessageTool: { key: "apps.telegram.tools.deleteMessage.enabled", defaultValue: false },
        tgPollTool: { key: "apps.telegram.tools.poll.enabled", defaultValue: false },
        tgPinTool: { key: "apps.telegram.tools.pin.enabled", defaultValue: false },
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
                            <Checkbox
                                className={s.thin}
                                disabled={loading || !!anyError}
                                checked={settings.tgRespondToKiriPrefix.value}
                                onChange={settings.tgRespondToKiriPrefix.setValue}
                                label={
                                    <FormLabel changed={settings.tgRespondToKiriPrefix.isChanged}>
                                        Respond when message starts with &quot;Kiri&quot; / &quot;Кири&quot;
                                    </FormLabel>
                                }
                            />
                            <h3>Agent tools</h3>
                            <Checkbox
                                className={s.thin}
                                disabled={loading || !!anyError}
                                checked={settings.tgReactionTool.value}
                                onChange={settings.tgReactionTool.setValue}
                                label={
                                    <FormLabel changed={settings.tgReactionTool.isChanged}>
                                        Allow reacting to messages
                                    </FormLabel>
                                }
                            />
                            <Checkbox
                                className={s.thin}
                                disabled={loading || !!anyError}
                                checked={settings.tgEditMessageTool.value}
                                onChange={settings.tgEditMessageTool.setValue}
                                label={
                                    <FormLabel changed={settings.tgEditMessageTool.isChanged}>
                                        Allow editing the bot&apos;s own messages
                                    </FormLabel>
                                }
                            />
                            <Checkbox
                                className={s.thin}
                                disabled={loading || !!anyError}
                                checked={settings.tgDeleteMessageTool.value}
                                onChange={settings.tgDeleteMessageTool.setValue}
                                label={
                                    <FormLabel changed={settings.tgDeleteMessageTool.isChanged}>
                                        Allow deleting the bot&apos;s own messages
                                    </FormLabel>
                                }
                            />
                            <Checkbox
                                className={s.thin}
                                disabled={loading || !!anyError}
                                checked={settings.tgPollTool.value}
                                onChange={settings.tgPollTool.setValue}
                                label={
                                    <FormLabel changed={settings.tgPollTool.isChanged}>
                                        Allow sending polls
                                    </FormLabel>
                                }
                            />
                            <Checkbox
                                className={s.thin}
                                disabled={loading || !!anyError}
                                checked={settings.tgPinTool.value}
                                onChange={settings.tgPinTool.setValue}
                                label={
                                    <FormLabel changed={settings.tgPinTool.isChanged}>
                                        Allow pinning messages
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
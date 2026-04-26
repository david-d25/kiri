import s from "./DonationSettings.module.scss";
import Container from "@/components/Container/Container";
import Button from "@/components/Button/Button";
import InfoPanel from "@/components/InfoPanel/InfoPanel";
import LoadingOverlay from "@/components/LoadingOverlay/LoadingOverlay";
import FormLabel from "@/components/FormLabel/FormLabel";
import TextInput from "@/components/TextInput/TextInput";
import TextArea from "@/components/TextArea/TextArea";
import NumberInput from "@/components/NumberInput/NumberInput";
import {useSettingsFormState} from "@/hooks/useSettingsFormState";

export default function DonationSettings() {
    const {
        settings,
        loading,
        anyError,
        anyChanged,
        clearErrors,
        save,
        reset,
    } = useSettingsFormState({
        ownerUsername: {
            key: "payments.ownerUsername",
            defaultValue: "",
        },
        cooldownHours: {
            key: "payments.minHoursBetweenInvoicesPerChat",
            defaultValue: 24,
        },
        termsText: {
            key: "payments.termsText",
            defaultValue: "",
        },
        supportText: {
            key: "payments.supportText",
            defaultValue: "",
        },
        paySupportText: {
            key: "payments.paySupportText",
            defaultValue: "",
        },
    });

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
                    <LoadingOverlay loading={loading}>
                        <div className={s.column}>
                            <div className={s.field}>
                                <FormLabel changed={settings.ownerUsername.isChanged}>Owner username</FormLabel>
                                <TextInput
                                    className={s.thin}
                                    value={settings.ownerUsername.value}
                                    onChange={settings.ownerUsername.setValue}
                                    disabled={loading}
                                    error={settings.ownerUsername.validationError}
                                    placeholder="username (without @)"
                                />
                                <div className={s.hint}>
                                    Substituted into {'{owner}'} in the texts below. Used by /support, /paysupport
                                    and /terms when those messages reference the owner.
                                </div>
                            </div>

                            <div className={s.field}>
                                <NumberInput
                                    className={s.thin}
                                    value={settings.cooldownHours.value}
                                    onChange={v => settings.cooldownHours.setValue(v ?? 0)}
                                    disabled={loading}
                                    error={settings.cooldownHours.validationError}
                                    minValue={0}
                                    step={1}
                                    label={
                                        <FormLabel changed={settings.cooldownHours.isChanged}>
                                            Cooldown between invoices per chat (hours)
                                        </FormLabel>
                                    }
                                />
                                <div className={s.hint}>
                                    The agent cannot send a new donation invoice to the same chat within this many
                                    hours of the previous one. Set to 0 to disable the cooldown.
                                </div>
                            </div>

                            <div className={s.field}>
                                <TextArea
                                    value={settings.termsText.value}
                                    onChange={settings.termsText.setValue}
                                    disabled={loading}
                                    error={settings.termsText.validationError}
                                    rows={10}
                                    resizable
                                    placeholder="Leave empty to use the built-in default."
                                    label={
                                        <FormLabel changed={settings.termsText.isChanged}>
                                            /terms text
                                        </FormLabel>
                                    }
                                />
                                <div className={s.hint}>
                                    Use {'{owner}'} as a placeholder for the owner username configured above.
                                </div>
                            </div>

                            <div className={s.field}>
                                <TextArea
                                    value={settings.supportText.value}
                                    onChange={settings.supportText.setValue}
                                    disabled={loading}
                                    error={settings.supportText.validationError}
                                    rows={4}
                                    resizable
                                    placeholder="Leave empty to use the built-in default."
                                    label={
                                        <FormLabel changed={settings.supportText.isChanged}>
                                            /support text
                                        </FormLabel>
                                    }
                                />
                            </div>

                            <div className={s.field}>
                                <TextArea
                                    value={settings.paySupportText.value}
                                    onChange={settings.paySupportText.setValue}
                                    disabled={loading}
                                    error={settings.paySupportText.validationError}
                                    rows={6}
                                    resizable
                                    placeholder="Leave empty to use the built-in default."
                                    label={
                                        <FormLabel changed={settings.paySupportText.isChanged}>
                                            /paysupport text
                                        </FormLabel>
                                    }
                                />
                            </div>
                        </div>
                    </LoadingOverlay>
                    <div className={s.row}>
                        <Button
                            colorAccent={'primary'}
                            disabled={!anyChanged || loading}
                            onClick={save}
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

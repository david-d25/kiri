import styles from "./IntegrationSettings.module.scss";
import Container from "@/components/Container/Container";
import {useGetSetting, useUpdateSetting} from "@/hooks/settings";
import {useEffect, useState} from "react";
import InfoPanel from "@/components/InfoPanel/InfoPanel";
import LoadingOverlay from "@/components/LoadingOverlay/LoadingOverlay";
import TextInput from "@/components/TextInput/TextInput";
import Button from "@/components/Button/Button";
import ExternalLink from "@/components/ExternalLink/ExternalLink";

export default function IntegrationSettings() {
    return (
        <div className={styles.root}>
            <Container>
                <div className={styles.body}>
                    <h2>GitHub</h2>
                    <div className={styles.settings}>
                        <Setting settingKey={'integration.github.apiKey'} label={'API key'} encrypt={true}/>
                        <ExternalLink href="https://github.com/settings/personal-access-tokens">
                            Create API key
                        </ExternalLink>
                    </div>

                    <h2>OpenAI</h2>
                    <div className={styles.settings}>
                        <Setting settingKey={'integration.openai.apiKey'} label={'API key'} encrypt={true}/>
                        <ExternalLink href="https://platform.openai.com/api-keys">
                            Create API key
                        </ExternalLink>
                    </div>

                    <h2>Anthropic</h2>
                    <div className={styles.settings}>
                        <Setting settingKey={'integration.anthropic.apiKey'} label={'API key'} encrypt={true}/>
                        <ExternalLink href="https://console.anthropic.com/settings/keys">
                            Create API key
                        </ExternalLink>
                    </div>

                    <h2>Google GenAI</h2>
                    <div className={styles.settings}>
                        <Setting settingKey={'integration.google.genAi.apiKey'} label={'API key'} encrypt={true}/>
                        <ExternalLink href="https://aistudio.google.com/api-keys">
                            Create API key
                        </ExternalLink>
                    </div>

                    {/*For future:*/}
                    {/*<h2>Discord</h2>*/}
                    {/*<Setting settingKey={'integration.discord.apiKey'} label={'API-ключ'} encrypt={true}/>*/}
                </div>
            </Container>
        </div>
    );
}

type SettingProps = {
    settingKey: string;
    label: string;
    encrypt: boolean;
}

function Setting(props: SettingProps) {
    const setting = useGetSetting(props.settingKey);
    const updateSettingRequest = useUpdateSetting(props.settingKey);
    const [ value, setValue ] = useState<string>("");

    const isLoading = setting.isLoading || updateSettingRequest.isPending;

    const [ errorMessage, setErrorMessage ] = useState<string | null>(null);

    useEffect(() => {
        if (setting.error) {
            setErrorMessage('Could not load: ' + setting.error.message);
        } else {
            setErrorMessage(null);
        }
    }, [setting.error]);

    useEffect(() => {
        if (setting.data) {
            setValue(setting.data.value || "");
        }
    }, [setting.data]);

    function onSaveClick() {
        updateSettingRequest.mutate({
            value: value ? value.toString() : null,
            encrypt: props.encrypt
        })
    }

    return (
        <div className={styles.setting}>
            { errorMessage && (
                <InfoPanel type="error">{errorMessage}</InfoPanel>
            ) }
            <LoadingOverlay loading={isLoading} className={styles.loadingOverlay}>
                <div className={styles.inputs}>
                    <div className={styles.row}>
                        <TextInput
                            type={props.encrypt ? "password" : "text"}
                            value={value}
                            onChange={setValue}
                            className={styles.input}
                            label={props.label}
                        />
                        <Button
                            onClick={onSaveClick}
                            colorAccent="primary"
                            className={styles.saveButton}
                            disabled={value === setting.data?.value}
                        >
                            Save
                        </Button>
                    </div>
                </div>
            </LoadingOverlay>
        </div>
    )
}
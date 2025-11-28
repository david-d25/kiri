import Header from "../components/Header/Header";
import PageLayout from "@/components/PageLayout/PageLayout";
import Head from "next/head";
import AgentSettings from "@/components/AgentSettings/AgentSettings";
import IntegrationSettings from "@/components/IntegrationSettings/IntegrationSettings";

export default function IntegrationsPage() {
    return (
        <PageLayout navBar={<Header/>}>
            <Head>
                <title>Integrations</title>
            </Head>
            <IntegrationSettings/>
        </PageLayout>
    );
}
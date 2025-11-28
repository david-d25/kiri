import Header from "../components/Header/Header";
import PageLayout from "@/components/PageLayout/PageLayout";
import Head from "next/head";
import AgentSettings from "@/components/AgentSettings/AgentSettings";

export default function AgentSettingsPage() {
    return (
        <PageLayout navBar={<Header/>}>
            <Head>
                <title>Agent Settings</title>
            </Head>
            <AgentSettings/>
        </PageLayout>
    );
}
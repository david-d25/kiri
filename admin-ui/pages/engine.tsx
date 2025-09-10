import Header from "../components/Header/Header";
import EngineControlPanel from "../components/EngineControlPanel/EngineControlPanel";
import PageLayout from "@/components/PageLayout/PageLayout";
import Head from "next/head";

export default function EnginePage() {
    return (
        <PageLayout navBar={<Header/>}>
            <Head>Engine</Head>
            <EngineControlPanel/>
        </PageLayout>
    );
}
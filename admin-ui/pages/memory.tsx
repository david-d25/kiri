import Header from "../components/Header/Header";
import PageLayout from "@/components/PageLayout/PageLayout";
import Head from "next/head";
import MemoryControl from "@/components/MemoryControl/MemoryControl";

export default function MemoryPage() {
    return (
        <PageLayout navBar={<Header/>}>
            <Head>
                <title>Memory</title>
            </Head>
            <MemoryControl/>
        </PageLayout>
    );
}
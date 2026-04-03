import Header from "../components/Header/Header";
import PageLayout from "@/components/PageLayout/PageLayout";
import Head from "next/head";
import AppsSettings from "@/components/AppsSettings/AppsSettings";

export default function AppsPage() {
    return (
        <PageLayout navBar={<Header/>}>
            <Head>
                <title>Apps</title>
            </Head>
            <AppsSettings/>
        </PageLayout>
    );
}
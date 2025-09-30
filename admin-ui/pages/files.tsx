import Header from "../components/Header/Header";
import PageLayout from "@/components/PageLayout/PageLayout";
import Head from "next/head";
import FilesControl from "@/components/FilesControl/FilesControl";

export default function MemoryPage() {
    return (
        <PageLayout navBar={<Header/>}>
            <Head>
                <title>Files</title>
            </Head>
            <FilesControl/>
        </PageLayout>
    );
}
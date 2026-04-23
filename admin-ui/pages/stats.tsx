import Head from "next/head";
import Header from "../components/Header/Header";
import PageLayout from "@/components/PageLayout/PageLayout";
import StatsPage from "@/components/StatsPage/StatsPage";

export default function Stats() {
    return (
        <PageLayout navBar={<Header/>}>
            <Head>
                <title>Stats</title>
            </Head>
            <StatsPage/>
        </PageLayout>
    );
}

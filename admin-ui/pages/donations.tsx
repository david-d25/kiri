import Header from "../components/Header/Header";
import DonationsControl from "../components/DonationsControl/DonationsControl";
import PageLayout from "@/components/PageLayout/PageLayout";
import Head from "next/head";

export default function DonationsPage() {
    return (
        <PageLayout navBar={<Header/>}>
            <Head>
                <title>Donations</title>
            </Head>
            <DonationsControl/>
        </PageLayout>
    );
}

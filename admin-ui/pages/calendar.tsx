import Header from "../components/Header/Header";
import PageLayout from "@/components/PageLayout/PageLayout";
import Head from "next/head";
import CalendarControl from "@/components/CalendarControl/CalendarControl";

export default function CalendarPage() {
    return (
        <PageLayout navBar={<Header/>}>
            <Head>
                <title>Calendar</title>
            </Head>
            <CalendarControl/>
        </PageLayout>
    );
}

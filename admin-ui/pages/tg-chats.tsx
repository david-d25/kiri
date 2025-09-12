import Header from "../components/Header/Header";
import TelegramChatsControl from "../components/TelegramChatsControl/TelegramChatsControl";
import PageLayout from "@/components/PageLayout/PageLayout";
import Head from "next/head";

export default function TelegramChatsPage() {
    return (
        <PageLayout navBar={<Header/>}>
            <Head>
                <title>Telegram Chats</title>
            </Head>
            <TelegramChatsControl/>
        </PageLayout>
    );
}
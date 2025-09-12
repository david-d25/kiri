import s from "./TelegramChatsControl.module.scss";
import {TelegramChatDto} from "@/lib/api/types/telegram/TelegramChatDto";
import {useSearchTelegramChats, useUpdateTelegramChatMetadata} from "@/hooks/telegram/telegramChats";
import {useEffect, useState} from "react";
import LoadingOverlay from "@/components/LoadingOverlay/LoadingOverlay";
import InfoPanel from "@/components/InfoPanel/InfoPanel";
import SkeletonLoader from "@/components/SkeletonLoader/SkeletonLoader";
import Container from "@/components/Container/Container";
import UserAvatar from "@/components/UserAvatar/UserAvatar";
import PageSelector from "@/components/PageSelector/PageSelector";
import Toggle from "@/components/Toggle/Toggle";
import {toastService} from "@/services/ToastService";

const PAGE_SIZE = 8;

export default function TelegramChatsControl() {
    const request = useSearchTelegramChats();
    const [pageIndex, setPageIndex] = useState(0);

    useEffect(() => {
        request.search({
            page: pageIndex,
            size: PAGE_SIZE,
        });
    }, [pageIndex]);

    return (
        <div className={s.root}>
            <Container>
                <div className={s.body}>
                    { maybeErrorMessage() }
                    <LoadingOverlay loading={request.isFetching}>
                        <div className={s.chatList}>
                            {
                                request.isLoading && !request.data && skeletonLoader()
                            }
                            {
                                request.data && chatList()
                            }
                        </div>
                    </LoadingOverlay>
                    <PageSelector pages={request.data?.totalPages} currentPage={pageIndex} onPageChange={setPageIndex}/>
                </div>
            </Container>
        </div>
    )

    function chatList(): JSX.Element {
        if (request.data?.content?.length > 0) {
            return request.data?.content?.map(chat => <TelegramChat key={chat.id} chat={chat}/>)
        } else {
            return <div className={s.noChats}>No chats</div>
        }
    }

    function skeletonLoader(): JSX.Element {
        return (
            <SkeletonLoader height={75} borderRadius={5} count={8}/>
        )
    }

    function maybeErrorMessage(): JSX.Element {
        if (request.isError) {
            return <InfoPanel type={'error'}>Loading error: {request.error.message}</InfoPanel>
        }
        return null;
    }
}

type TelegramChatProps = {
    chat: TelegramChatDto
}

function TelegramChat(props: TelegramChatProps) {
    const updateReq = useUpdateTelegramChatMetadata();

    const chat = props.chat;
    const name = chat.firstName || chat.title || '(unknown)';

    useEffect(() => {
        if (updateReq.error) {
            toastService.error(updateReq.error.message);
        }
    }, [updateReq.error]);

    function onEnabledChange(value: boolean) {
        updateReq.mutate({
            chatId: chat.id,
            enabled: value
        });
    }

    return (
        <div className={s.chat}>
            <UserAvatar src={chat.smallPhotoUrl} alt={'Avatar'} name={name}/>
            <div className={s.nameAndUsername}>
                <div className={s.name}>{name}</div>
                {
                    chat.username && <div className={s.username}>@{chat.username}</div>
                }
            </div>
            <div className={s.controls}>
                <Toggle checked={props.chat.metadata.enabled}
                        onChange={onEnabledChange}
                        disabled={updateReq.isPending}
                        label={'Enabled'}
                />
            </div>
        </div>
    )
}
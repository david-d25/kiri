import {useEffect, useRef, useState} from "react";
import s from "./CalendarControl.module.scss";
import Container from "@/components/Container/Container";
import TextInput from "@/components/TextInput/TextInput";
import Button from "@/components/Button/Button";
import LoadingOverlay from "@/components/LoadingOverlay/LoadingOverlay";
import SkeletonLoader from "@/components/SkeletonLoader/SkeletonLoader";
import InfoPanel from "@/components/InfoPanel/InfoPanel";
import PageSelector from "@/components/PageSelector/PageSelector";
import CalendarEventCard from "@/components/CalendarEventCard/CalendarEventCard";
import CalendarCreateModal, {CalendarCreateResult} from "@/components/CalendarCreateModal/CalendarCreateModal";
import {modalService} from "@/services/ModalService";
import {toastService} from "@/services/ToastService";
import {useSearchCalendarEvents, useCreateCalendarEvent} from "@/hooks/calendar";

const PAGE_SIZE = 20;

export default function CalendarControl() {
    const [searchText, setSearchText] = useState("");
    const [pageIndex, setPageIndex] = useState(0);

    const eventsRequest = useSearchCalendarEvents();
    const createMutation = useCreateCalendarEvent();

    const refreshListRef = useRef(() => {});
    refreshListRef.current = () => {
        eventsRequest.search({search: searchText || undefined, page: pageIndex, size: PAGE_SIZE});
    };

    useEffect(() => {
        eventsRequest.search({search: searchText || undefined, page: pageIndex, size: PAGE_SIZE});
    }, [pageIndex, searchText]);

    function handleSearchChange(value: string) {
        setSearchText(value);
        setPageIndex(0);
    }

    useEffect(() => {
        if (createMutation.error) toastService.error(createMutation.error.message);
    }, [createMutation.error]);

    useEffect(() => {
        if (createMutation.isSuccess) {
            toastService.success("Event created");
            refreshListRef.current();
        }
    }, [createMutation.isSuccess]);

    async function handleCreate() {
        const result = await modalService.open<CalendarCreateResult>({
            title: "Create Calendar Event",
            content: CalendarCreateModal,
            closeOnBackdropClick: true,
            closeOnEscape: true,
        });
        if (result) {
            createMutation.mutate(result);
        }
    }

    function handleEventChanged() {
        refreshListRef.current();
    }

    return (
        <div className={s.root}>
            <Container>
                <div className={s.body}>
                    <div className={s.toolbar}>
                        <TextInput
                            value={searchText}
                            onChange={handleSearchChange}
                            placeholder="Filter by title..."
                            className={s.searchInput}
                        />
                        <div className={s.controls}>
                            <Button onClick={handleCreate} colorAccent="primary">
                                + Create
                            </Button>
                        </div>
                    </div>

                    {eventsRequest.isError && (
                        <InfoPanel type="error">Error: {eventsRequest.error.message}</InfoPanel>
                    )}

                    <LoadingOverlay loading={eventsRequest.isFetching}>
                        <div className={s.eventList}>
                            {eventsRequest.isLoading && !eventsRequest.data && (
                                <SkeletonLoader height={60} borderRadius={5} count={6}/>
                            )}
                            {eventsRequest.data && eventsRequest.data.content.length === 0 && (
                                <div className={s.empty}>No calendar events</div>
                            )}
                            {eventsRequest.data?.content.map(event => (
                                <CalendarEventCard
                                    key={event.id}
                                    event={event}
                                    onChanged={handleEventChanged}
                                />
                            ))}
                        </div>
                    </LoadingOverlay>

                    {eventsRequest.data && eventsRequest.data.totalPages > 1 && (
                        <PageSelector
                            pages={eventsRequest.data.totalPages}
                            currentPage={pageIndex}
                            onPageChange={setPageIndex}
                        />
                    )}
                </div>
            </Container>
        </div>
    );
}

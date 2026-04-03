import {JSX, useCallback, useEffect, useRef, useState} from "react";
import s from "./MemoryControl.module.scss";
import Container from "@/components/Container/Container";
import TextInput from "@/components/TextInput/TextInput";
import Button from "@/components/Button/Button";
import LoadingOverlay from "@/components/LoadingOverlay/LoadingOverlay";
import SkeletonLoader from "@/components/SkeletonLoader/SkeletonLoader";
import InfoPanel from "@/components/InfoPanel/InfoPanel";
import PageSelector from "@/components/PageSelector/PageSelector";
import MemoryPointCard from "@/components/MemoryPointCard/MemoryPointCard";
import MemoryCreateModal, {MemoryCreateResult} from "@/components/MemoryCreateModal/MemoryCreateModal";
import {modalService} from "@/services/ModalService";
import {toastService} from "@/services/ToastService";
import {
    useMemoryStats,
    useSearchMemoryPoints,
    useSemanticSearch,
    useCreateMemoryPoint
} from "@/hooks/memory";
import Toggle from "@/components/Toggle/Toggle";

const PAGE_SIZE = 15;

export default function MemoryControl() {
    const [searchText, setSearchText] = useState("");
    const [semanticMode, setSemanticMode] = useState(false);
    const [pageIndex, setPageIndex] = useState(0);

    const stats = useMemoryStats();
    const pointsRequest = useSearchMemoryPoints();
    const semanticSearch = useSemanticSearch();
    const createMutation = useCreateMemoryPoint();

    const refreshListRef = useRef(() => {});
    refreshListRef.current = () => {
        if (semanticMode) {
            semanticSearch.reset();
        } else {
            pointsRequest.search({ search: searchText || undefined, page: pageIndex, size: PAGE_SIZE });
        }
        stats.refetch();
    };

    // Load points on page/search change (text mode)
    useEffect(() => {
        if (!semanticMode) {
            pointsRequest.search({
                search: searchText || undefined,
                page: pageIndex,
                size: PAGE_SIZE,
            });
        }
    }, [pageIndex, searchText, semanticMode]);

    // Reset page on search text change
    useEffect(() => {
        setPageIndex(0);
    }, [searchText]);

    useEffect(() => {
        if (createMutation.error) toastService.error(createMutation.error.message);
    }, [createMutation.error]);

    useEffect(() => {
        if (createMutation.isSuccess) {
            toastService.success("Memory created");
            refreshListRef.current();
        }
    }, [createMutation.isSuccess]);

    const handleSemanticSearch = useCallback(() => {
        if (searchText.trim() && semanticMode) {
            semanticSearch.mutate({ query: searchText.trim(), limit: PAGE_SIZE });
        }
    }, [searchText, semanticMode]);

    function handleKeyDown(e: React.KeyboardEvent) {
        if (e.key === 'Enter' && semanticMode && searchText.trim()) {
            handleSemanticSearch();
        }
    }

    async function handleCreate() {
        const result = await modalService.open<MemoryCreateResult>({
            title: "Create Memory",
            content: MemoryCreateModal,
            closeOnBackdropClick: true,
            closeOnEscape: true,
        });
        if (result) {
            createMutation.mutate({ value: result.value, keys: result.keys });
        }
    }

    function handlePointDeleted() {
        refreshListRef.current();
    }

    return (
        <div className={s.root}>
            <Container>
                <div className={s.body}>
                    {stats.data && (
                        <div className={s.statsBar}>
                            <span className={s.statItem}>
                                <span className={s.statValue}>{stats.data.totalPoints}</span> points
                            </span>
                            <span className={s.statItem}>
                                <span className={s.statValue}>{stats.data.totalKeys}</span> keys
                            </span>
                            <span className={s.statItem}>
                                <span className={s.statValue}>{stats.data.totalLinks}</span> links
                            </span>
                        </div>
                    )}

                    <div className={s.toolbar}>
                        <div className={s.searchRow}>
                            <TextInput
                                value={searchText}
                                onChange={setSearchText}
                                placeholder={semanticMode ? "Semantic search query..." : "Filter by text..."}
                                className={s.searchInput}
                                onKeyDown={handleKeyDown}
                            />
                            {semanticMode && (
                                <Button
                                    onClick={handleSemanticSearch}
                                    colorAccent="primary"
                                    disabled={!searchText.trim() || semanticSearch.isPending}
                                >
                                    Search
                                </Button>
                            )}
                        </div>
                        <div className={s.controls}>
                            <Toggle
                                checked={semanticMode}
                                onChange={setSemanticMode}
                                label="Semantic"
                            />
                            <Button onClick={handleCreate} colorAccent="primary">
                                + Create
                            </Button>
                        </div>
                    </div>

                    {pointsRequest.isError && !semanticMode && (
                        <InfoPanel type="error">Error: {pointsRequest.error.message}</InfoPanel>
                    )}
                    {semanticSearch.isError && semanticMode && (
                        <InfoPanel type="error">Error: {semanticSearch.error.message}</InfoPanel>
                    )}

                    {!semanticMode && renderTextSearchResults()}
                    {semanticMode && renderSemanticResults()}
                </div>
            </Container>
        </div>
    );

    function renderTextSearchResults(): JSX.Element {
        return (
            <>
                <LoadingOverlay loading={pointsRequest.isFetching}>
                    <div className={s.pointList}>
                        {pointsRequest.isLoading && !pointsRequest.data && (
                            <SkeletonLoader height={55} borderRadius={5} count={8}/>
                        )}
                        {pointsRequest.data && pointsRequest.data.content.length === 0 && (
                            <div className={s.empty}>No memory points found</div>
                        )}
                        {pointsRequest.data?.content.map(point => (
                            <MemoryPointCard
                                key={point.id}
                                point={point}
                                onDeleted={handlePointDeleted}
                            />
                        ))}
                    </div>
                </LoadingOverlay>
                {pointsRequest.data && pointsRequest.data.totalPages > 1 && (
                    <PageSelector
                        pages={pointsRequest.data.totalPages}
                        currentPage={pageIndex}
                        onPageChange={setPageIndex}
                    />
                )}
            </>
        );
    }

    function renderSemanticResults(): JSX.Element {
        if (semanticSearch.isPending) {
            return (
                <div className={s.pointList}>
                    <SkeletonLoader height={55} borderRadius={5} count={5}/>
                </div>
            );
        }

        if (!semanticSearch.data && !semanticSearch.isError) {
            return (
                <div className={s.empty}>
                    Enter a query and press Search to find semantically similar memories
                </div>
            );
        }

        if (semanticSearch.data && semanticSearch.data.length === 0) {
            return <div className={s.empty}>No results found</div>;
        }

        return (
            <div className={s.pointList}>
                {semanticSearch.data?.map(result => (
                    <MemoryPointCard
                        key={result.point.id}
                        point={result.point}
                        score={result.score}
                        onDeleted={handlePointDeleted}
                    />
                ))}
            </div>
        );
    }
}

import {JSX, useEffect, useRef, useState} from "react";
import s from "./MemoryPointCard.module.scss";
import {MemoryPointDto} from "@/lib/api/types/memory/MemoryPointDto";
import {MemoryLinkDto} from "@/lib/api/types/memory/MemoryLinkDto";
import {
    useGetMemoryPointDetail,
    useUpdateMemoryPoint,
    useDeleteMemoryPoint,
    useDeleteMemoryLink,
    useUpdateMemoryLink
} from "@/hooks/memory";
import Button from "@/components/Button/Button";
import TextInput from "@/components/TextInput/TextInput";
import NumberInput from "@/components/NumberInput/NumberInput";
import {toastService} from "@/services/ToastService";

type Props = {
    point: MemoryPointDto;
    score?: number;
    onDeleted?: () => void;
};

export default function MemoryPointCard({ point, score, onDeleted }: Props) {
    const [expanded, setExpanded] = useState(false);
    const [editing, setEditing] = useState(false);
    const [editValue, setEditValue] = useState(point.value);
    const detail = useGetMemoryPointDetail(expanded ? point.id : null);
    const updateMutation = useUpdateMemoryPoint();
    const deleteMutation = useDeleteMemoryPoint();
    const deleteLinkMutation = useDeleteMemoryLink();
    const updateLinkMutation = useUpdateMemoryLink();
    const cancelEditRef = useRef(false);

    useEffect(() => {
        if (updateMutation.error) toastService.error(updateMutation.error.message);
    }, [updateMutation.error]);

    useEffect(() => {
        if (deleteMutation.error) toastService.error(deleteMutation.error.message);
    }, [deleteMutation.error]);

    useEffect(() => {
        if (deleteMutation.isSuccess && onDeleted) onDeleted();
    }, [deleteMutation.isSuccess]);

    function handleSaveEdit() {
        if (cancelEditRef.current) { cancelEditRef.current = false; return; }
        if (!editValue.trim()) return;
        updateMutation.mutate({ id: point.id, value: editValue.trim() });
        setEditing(false);
    }

    function handleDelete() {
        if (confirm('Delete this memory point and all its links?')) {
            deleteMutation.mutate({ id: point.id });
        }
    }

    function handleDeleteLink(link: MemoryLinkDto) {
        deleteLinkMutation.mutate({ keyId: link.memoryKeyId, pointId: link.memoryPointId });
    }

    function handleWeightChange(link: MemoryLinkDto, newWeight: number) {
        updateLinkMutation.mutate({
            keyId: link.memoryKeyId,
            pointId: link.memoryPointId,
            weight: Math.max(0.01, Math.min(2.0, newWeight))
        });
    }

    const createdDate = new Date(Number(point.createdAt) * 1000).toLocaleString();

    return (
        <div className={s.card}>
            <div className={s.header} onClick={() => setExpanded(!expanded)}>
                <div className={s.mainInfo}>
                    {score !== undefined && (
                        <span className={s.score} title="Similarity score">
                            {score.toFixed(2)}
                        </span>
                    )}
                    {editing ? (
                        <div onClick={e => e.stopPropagation()} className={s.editInputWrap}>
                            <TextInput
                                className={s.editInput}
                                value={editValue}
                                onChange={setEditValue}
                                onKeyDown={e => {
                                    if (e.key === 'Enter') handleSaveEdit();
                                    if (e.key === 'Escape') { cancelEditRef.current = true; setEditing(false); setEditValue(point.value); }
                                }}
                                onBlur={handleSaveEdit}
                            />
                        </div>
                    ) : (
                        <span className={s.value}>{point.value}</span>
                    )}
                </div>
                <div className={s.meta}>
                    <span className={s.keysCount}>{point.linkedKeysCount} keys</span>
                    <span className={s.date}>{createdDate}</span>
                    <div className={s.actions} onClick={e => e.stopPropagation()}>
                        <Button noStyle onClick={() => { setEditing(true); setEditValue(point.value); }}>
                            Edit
                        </Button>
                        <Button noStyle onClick={handleDelete}>
                            Delete
                        </Button>
                    </div>
                </div>
            </div>
            {expanded && (
                <div className={s.details}>
                    {detail.isLoading && <div className={s.loading}>Loading links...</div>}
                    {detail.data && detail.data.links.length === 0 && (
                        <div className={s.noLinks}>No linked keys</div>
                    )}
                    {detail.data && detail.data.links.map(link => (
                        <LinkItem
                            key={`${link.memoryKeyId}-${link.memoryPointId}`}
                            link={link}
                            onDelete={() => handleDeleteLink(link)}
                            onWeightChange={(w) => handleWeightChange(link, w)}
                        />
                    ))}
                </div>
            )}
        </div>
    );
}

type LinkItemProps = {
    link: MemoryLinkDto;
    onDelete: () => void;
    onWeightChange: (weight: number) => void;
};

function LinkItem({ link, onDelete, onWeightChange }: LinkItemProps): JSX.Element {
    const weightPercent = Math.min(100, (link.weight / 2.0) * 100);
    const hue = (link.weight / 2.0) * 120; // 0=red, 120=green

    return (
        <div className={s.linkItem}>
            <span className={s.keyText}>{link.keyText || link.memoryKeyId}</span>
            <div className={s.weightContainer}>
                <div className={s.weightBar}>
                    <div
                        className={s.weightFill}
                        style={{
                            width: `${weightPercent}%`,
                            background: `oklch(60% 0.15 ${hue})`
                        }}
                    />
                </div>
                <NumberInput
                    value={link.weight}
                    onChange={v => onWeightChange(v ?? 0.01)}
                    minValue={0.01}
                    maxValue={2.0}
                    step={0.01}
                    className={s.weightInputContainer}
                    inputClassName={s.weightInput}
                />
            </div>
            <Button noStyle onClick={onDelete} className={s.deleteLinkBtn}>
                ×
            </Button>
        </div>
    );
}

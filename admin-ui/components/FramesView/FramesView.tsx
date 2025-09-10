import s from './FramesView.module.scss';
import {classnames} from "@/lib/classnames";
import {FrameDto} from "@/lib/api/types/FrameDto";
import Frame from "@/components/Frame/Frame";
import {useEffect, useRef, useState} from "react";
import Checkbox from "@/components/Checkbox/Checkbox";

type FramesViewProps = {
    frames: FrameDto[];
    className?: string;
    title: string;
};

export default function FramesView(props: FramesViewProps) {
    const [ autoscroll, setAutoscroll ] = useState(true);

    return (
        <div className={classnames(s.root, props.className)}>
            <div className={s.header}>
                <div className={s.title}>{props.title}</div>
                <Checkbox label={'Autoscroll'} checked={autoscroll} onChange={setAutoscroll}/>
            </div>
            <div className={s.body}>
                <FramesList frames={props.frames} autoscroll={autoscroll}/>
            </div>
        </div>
    );
}


type FramesListProps = {
    frames: FrameDto[];
    autoscroll: boolean;
};

function FramesList({ frames, autoscroll }: FramesListProps) {
    const listRef = useRef<HTMLDivElement | null>(null);

    useEffect(() => {
        if (!autoscroll) return;
        const el = listRef.current;
        if (!el) return;
        el.scrollTop = el.scrollHeight;
    }, [frames[0], autoscroll]);

    return (
        <div className={s.framesList} ref={listRef}>
            {frames.map((frame, i) => (
                <Frame key={(frame as any)?.id ?? i} frame={frame}/>
            ))}
        </div>
    );
}
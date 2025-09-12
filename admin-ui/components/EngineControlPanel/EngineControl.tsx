import s from './EngineControlPanel.module.scss';
import EngineStatusBar from "../EngineStatusBar/EngineStatusBar";
import FramesView from "../FramesView/FramesView";
import {useSSEEvent} from "@/hooks/useSSEEvent";
import {FrameBufferStateDto} from "@/lib/api/types/FrameBufferStateDto";

export default function EngineControl() {
    const stateSse = useSSEEvent("frameBufferState", FrameBufferStateDto.NULL);

    return (
        <div className={s.root}>
            <EngineStatusBar className={s.statusBar} />
            <FramesView className={s.fixedFramesView} title="Fixed frames" frames={stateSse.fixedFrames}/>
            <FramesView className={s.rollingFramesView} title="Rolling frames" frames={stateSse.rollingFrames}/>
        </div>
    )
}
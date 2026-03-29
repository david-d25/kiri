import s from './EngineControlPanel.module.scss';
import EngineStatusBar from "../EngineStatusBar/EngineStatusBar";
import FramesView from "../FramesView/FramesView";
import ToolPanel from "../ToolPanel/ToolPanel";
import {useSSEEvent} from "@/hooks/useSSEEvent";
import {FrameBufferStateDto} from "@/lib/api/types/FrameBufferStateDto";

export default function EngineControl() {
    const stateSse = useSSEEvent("frameBufferState", FrameBufferStateDto.NULL);

    return (
        <div className={s.root}>
            <EngineStatusBar className={s.statusBar} />
            <ToolPanel className={s.toolPanel} />
            <FramesView className={s.framesView} title="Frames" frames={stateSse.frames}/>
        </div>
    )
}
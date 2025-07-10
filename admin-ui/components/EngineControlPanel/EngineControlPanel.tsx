import s from './EngineControlPanel.module.scss';
import EngineStatusBar from "../EngineStatusBar/EngineStatusBar";
import FramesView from "../FramesView/FramesView";

export default function EngineControlPanel() {
    return (
        <div className={s.root}>
            <EngineStatusBar className={s.statusBar} />
            <FramesView className={s.fixedFramesView} title="Fixed frames"/>
            <FramesView className={s.rollingFramesView} title="Rolling frames"/>
        </div>
    )
}
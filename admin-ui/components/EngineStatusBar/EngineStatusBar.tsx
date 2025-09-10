import s from './EngineStatusBar.module.scss';
import {classnames} from "@/lib/classnames";
import Panel from "@/components/Panel/Panel";
import {useSSEEvent} from "@/hooks/useSSEEvent";
import {useHardStopAgent, useStartAgent, useStopAgent, useTickAgent} from "@/hooks/agent";
import Button from "@/components/Button/Button";

type Props = {
    className?: string;
};

export default function EngineStatusBar(props: Props) {
    const stateSse = useSSEEvent("engineState", "PAUSED");
    const startRequest = useStartAgent();
    const stopRequest = useStopAgent();
    const tickRequest = useTickAgent();
    const hardStopRequest = useHardStopAgent();

    return (
       <Panel className={classnames(s.root, props.className)}>
           <span className={s.title}>Engine</span>
           <span className={s.statusDot} data-status={stateSse}></span>
           <span className={s.stateText}>{stateSse}</span>

           <Button onClick={() => startRequest.mutate(undefined)}>Start</Button>
           <Button onClick={() => tickRequest.mutate(undefined)}>Tick</Button>
           <Button onClick={() => stopRequest.mutate(undefined)}>Stop</Button>
           <Button onClick={() => hardStopRequest.mutate(undefined)}>Hard Stop</Button>
       </Panel>
    )
}
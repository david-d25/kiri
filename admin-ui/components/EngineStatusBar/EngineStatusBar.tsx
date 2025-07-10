import s from './EngineStatusBar.module.scss';
import {classnames} from "../../lib/classnames";

type Props = {
    className?: string;
};

export default function EngineStatusBar(props: Props) {
   return (
       <div className={classnames(s.root, props.className)}>
           engine status
       </div>
   )
}
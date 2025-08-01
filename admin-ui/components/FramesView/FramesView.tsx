import s from './FramesView.module.scss';
import {classnames} from "../../lib/classnames";

type Props = {
    className?: string;
    title: string;
};

export default function FramesView(props: Props) {
    return (
        <div className={classnames(s.root, props.className)}>
            frames view
        </div>
    );
}

import {DataFrameDto, FrameDto, ToolCallFrameDto} from "../../lib/api/types/FrameDto";

import s from './Frame.module.scss';

type Props = {
    frame: FrameDto
};

export default function Frame(props: Props) {
    const { frame } = props;
    if (frame.type === 'data') {
        renderDataFrame(frame);
    } else if (frame.type === 'toolCall') {
        renderToolCallFrame(frame);
    }
}

function renderDataFrame(frame: DataFrameDto) {
    return (
        <div className={s.frame}>
            <div className={s.head}>
                <div className={s.tag}>
                    {frame.tag}
                </div>
            </div>
            <div className={s.content}>
                todo
            </div>
        </div>
    );
}

function renderToolCallFrame(frame: ToolCallFrameDto) {
    return (
        <div className={s.frame}>
            todo
        </div>
    );
}
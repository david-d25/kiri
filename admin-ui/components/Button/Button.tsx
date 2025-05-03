import style from './Button.module.scss'
import React, {MouseEventHandler} from "react";
import {classnames} from "../../lib/classnames";

type Props = {
    children?: React.ReactNode,
    disabled?: boolean,
    lightweight?: boolean,
    onClick?: MouseEventHandler<HTMLButtonElement>,
};

export default function Button(props: Props) {
    return (
        <button
            tabIndex={0}
            className={classnames({[style.button]: true, [style.lightweight]: props.lightweight})}
            onClick={props.onClick}
            disabled={props.disabled}
        >
            {props.children}
        </button>
    )
}
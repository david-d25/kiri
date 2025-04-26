import style from './Button.module.scss'
import React, {MouseEventHandler} from "react";

type Props = {
    children?: React.ReactNode,
    disabled?: boolean,
    onClick?: MouseEventHandler<HTMLButtonElement>,
};

export default function Button(props: Props) {
    return (
        <button
            tabIndex={0}
            className={style.button}
            onClick={props.onClick}
            disabled={props.disabled}
        >
            {props.children}
        </button>
    )
}
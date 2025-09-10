import style from './Button.module.scss'
import React, {MouseEventHandler} from "react";
import {classnames} from "@/lib/classnames";

type Props = {
    children?: React.ReactNode,
    disabled?: boolean,
    lightweight?: boolean,
    onClick?: MouseEventHandler<HTMLButtonElement>,
    ref?: React.Ref<HTMLButtonElement>,
    className?: string
    type?: 'button' | 'submit' | 'reset',
    colorAccent?: 'primary' | 'default'
};

export default function Button(props: Props) {
    const className = classnames(
        {
            [style.button]: true,
            [style.lightweight]: props.lightweight,
            [style.primary]: props.colorAccent === 'primary',
        },
        props.className
    );
    return (
        <button
            type={props.type || 'button'}
            tabIndex={0}
            className={className}
            onClick={props.onClick}
            disabled={props.disabled}
            ref={props.ref}
        >
            {props.children}
        </button>
    )
}
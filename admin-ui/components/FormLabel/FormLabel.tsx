import s from "./FormLabel.module.scss";

import React from "react";

type Props = {
    children: React.ReactNode;
    changed?: boolean;
};

export default function FormLabel(props: Props) {
    return (
        <label className={s.root} data-changed={props.changed}>
            <span>{props.children}</span>
            { props.changed && (
                <>
                    <span> </span>
                    <span className={s.changedMark}>*</span>
                </>
            ) }
        </label>
    )
}
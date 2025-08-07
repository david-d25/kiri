import styles from "./Container.module.scss";
import React from "react";
import {classnames} from "@/lib/classnames";

type Props = {
    className?: string;
    children?: React.ReactNode;
};

export default function Container(props: Props) {
    return (
        <div className={classnames(styles.root, props.className)}>
            {props.children}
        </div>
    )
}
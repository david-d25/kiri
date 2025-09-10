import React from "react";
import {classnames} from "@/lib/classnames";
import styles from './Panel.module.scss';

export type PanelProps = {
    children?: React.ReactNode;
    className?: string;
};

export default function Panel(props: PanelProps) {
    return (
        <div className={classnames(styles.root, props.className)}>
            {props.children}
        </div>
    )
}
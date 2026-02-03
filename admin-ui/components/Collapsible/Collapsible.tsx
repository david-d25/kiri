import s from "./Collapsible.module.scss";

import React from "react";

import RightChevronIcon from "@/icons/chevron-right.svg"
import {classnames} from "@/lib/classnames";
import {AnimatePresence, motion} from "framer-motion";
import {defaultTransition} from "@/lib/motionUtils";

type CollapsibleProps = {
    header?: React.ReactNode;
    children?: React.ReactNode;
    className?: string;
    /**
     * If provided, controls the opened state of the collapsible.
     * If not provided, the component manages its own state.
     */
    opened?: boolean;
    onOpenChange?: (opened: boolean) => void;
};

export default function (props: CollapsibleProps) {
    const [ internalOpened, setInternalOpened ] = React.useState(false);

    function onHeaderClick() {
        if (props.opened !== undefined) {
            props.onOpenChange?.(!props.opened);
        } else {
            setInternalOpened(!internalOpened);
            props.onOpenChange?.(!internalOpened);
        }
    }

    const effectivelyOpened = props.opened !== undefined ? props.opened : internalOpened;

    return (
        <div className={classnames(s.root, props.className)} data-opened={effectivelyOpened}>
            <div className={s.header} onClick={onHeaderClick}>
                <RightChevronIcon className={s.icon}/>
                <div className={s.headerContent}>
                    {props.header}
                </div>
            </div>
            <AnimatePresence initial={false}>
                {effectivelyOpened && (
                    <motion.div
                        className={s.childrenWrapper}
                        key="content"
                        initial={{ opacity: 0, height: 0 }}
                        animate={{ opacity: 1, height: "auto" }}
                        exit={{ opacity: 0, height: 0 }}
                        transition={defaultTransition}
                        layout={'position'}
                    >
                        <div className={s.children}>
                            {props.children}
                        </div>
                    </motion.div>
                )}
            </AnimatePresence>
        </div>
    )
}
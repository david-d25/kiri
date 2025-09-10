import styles from "./ExternalLink.module.scss";

import Link, {LinkProps} from "next/link";
import ExternalLinkIcon from "@/icons/external-link.svg";
import {classnames} from "@/lib/classnames";
import {ReactNode} from "react";

type ExternalLinkProps = LinkProps & {
    children?: ReactNode;
    className?: string;
}

export default function ExternalLink(props: ExternalLinkProps) {
    return (
        <div className={classnames(styles.root, props.className)}>
            <Link {...props} target={"_blank"} className={styles.link}>
                {props.children}
                <ExternalLinkIcon className={styles.icon}/>
            </Link>
        </div>
    )
}
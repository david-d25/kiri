import styles from "./PageLayout.module.scss";
import {ReactNode} from "react";

type Props = {
    navBar: ReactNode;
    children: ReactNode;
};

export default function PageLayout(props: Props) {
    return (
        <div className={styles.root}>
            { props.navBar && (
                <div className={styles.header}>
                    { props.navBar }
                </div>
            ) }
            <div className={styles.main}>
                { props.children }
            </div>
        </div>
    )
}
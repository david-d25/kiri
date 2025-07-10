import s from './HeaderNavButton.module.scss';
import React from "react";
import Link from "next/link";
import {useRouter} from "next/router";
import {classnames} from "../../lib/classnames";

type Props = {
    url: string;
    children: React.ReactNode;
}

export default function HeaderNavButton(props: Props) {
    const router = useRouter();

    const active = router.asPath === props.url;
    const className = classnames({
        [s.headerNavButton]: true,
        [s.active]: active,
    })
    return (
        <Link href={props.url} className={className}>
            {props.children}
        </Link>
    );
}
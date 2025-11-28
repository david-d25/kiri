import s from "./TabNavigation.module.scss";
import React from "react";
import {classnames} from "@/lib/classnames";

type Tab<T> = {
    id: T;
    title: React.ReactNode;
    disabled?: boolean;
}

export type TabNavigationProps<T> = {
    tabs: Tab<T>[];
    tabId: T;
    onTabChange: (tab: T) => void;
};

export default function TabNavigation<T extends string>(props: TabNavigationProps<T>) {
    return (
        <div className={s.tabButtons}>
            { props.tabs.map(tab => (
                <button
                    key={tab.id}
                    className={classnames({
                        [s.tabButton]: true,
                        [s.active]: props.tabId == tab.id
                    })}
                    disabled={tab.disabled}
                    onClick={() => props.onTabChange(tab.id)}>
                    { tab.title }
                </button>
            )) }
        </div>
    )
}
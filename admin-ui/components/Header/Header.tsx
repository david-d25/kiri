import s from './Header.module.scss';
import Logo from "../Logo/Logo";
import HeaderUserCard from "../HeaderUserCard/HeaderUserCard";
import HeaderNavButton from "../HeaderNavButton/HeaderNavButton";

export default function Header() {
    return (
        <header className={s.header}>
            <div className={s.left}>
                <Logo/>
            </div>
            <div className={s.center}>
                <HeaderNavButton url="/engine">Engine</HeaderNavButton>
                <HeaderNavButton url="/memory">Memory</HeaderNavButton>
                <HeaderNavButton url="/tg-chats">Telegram Chats</HeaderNavButton>
            </div>
            <div className={s.right}>
                <HeaderUserCard />
            </div>
        </header>
    )
}
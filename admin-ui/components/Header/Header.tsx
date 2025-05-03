import s from './Header.module.scss';
import Logo from "../Logo/Logo";
import HeaderUserCard from "../HeaderUserCard/HeaderUserCard";

export default function Header() {
    return (
        <header className={s.header}>
            <div className={s.left}>
                <Logo/>
            </div>
            <div className={s.center}>

            </div>
            <div className={s.right}>
                <HeaderUserCard />
            </div>
        </header>
    )
}
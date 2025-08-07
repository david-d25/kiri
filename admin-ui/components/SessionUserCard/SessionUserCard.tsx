import s from './SessionUserCard.module.scss';
import {useBootstrap} from "@/hooks/bootstrap";
import SkeletonLoader from "../SkeletonLoader/SkeletonLoader";
import Button from "../Button/Button";
import UserAvatar from "../UserAvatar/UserAvatar";
import {useRouter} from "next/router";
import {useLogout} from "@/hooks/logout";

export default function SessionUserCard() {
    const { data, error, isLoading } = useBootstrap();
    const router = useRouter();
    const logoutMutation = useLogout();

    if (error) {
        return (
            <div className={s.userCard}>Network error</div>
        )
    }

    if (isLoading) {
        return (
            <div className={s.userCard}>
                <SkeletonLoader width={40} className={s.skeletonLoader} type="circle"/>
                <SkeletonLoader className={s.skeletonLoader} type="text"/>
            </div>
        )
    }

    if (!data) {
        return;
    }

    function onLoginClick() {
        router.push('/login').then(() => {}).catch(console.error);
    }

    function onLogoutClick() {
        logoutMutation.mutate(undefined, {
            onSuccess: () => router.replace("/login")
        });
    }


    if (!data.isAuthenticated) {
        return (
            <div className={s.userCard}>
                <Button className={s.loginButton} lightweight={true} onClick={onLoginClick}>
                    Войти
                </Button>
            </div>
        );
    }

    const user = data!!.user!!;
    const name = user.firstName + (user.lastName ? ` ${user.lastName}` : '');
    return (
        <div className={s.userCard}>
            <UserAvatar src={user.photoUrl} size="small" alt={`${user.firstName}'s avatar`} name={name} />
            <span className={s.userName}>
                {name}
            </span>
            <Button className={s.logoutButton} lightweight={true} onClick={onLogoutClick}>
                Выйти
            </Button>
        </div>
    )
}
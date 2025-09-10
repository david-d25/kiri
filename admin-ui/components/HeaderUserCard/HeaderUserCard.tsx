import s from './HeaderUserCard.module.scss';
import {useBootstrap} from "@/hooks/bootstrap";
import SkeletonLoader from "../SkeletonLoader/SkeletonLoader";
import Button from "../Button/Button";
import UserAvatar from "../UserAvatar/UserAvatar";
import {useRouter} from "next/router";
import {useLogout} from "@/hooks/logout";

export default function HeaderUserCard() {
    const { data, error, isLoading } = useBootstrap();
    const router = useRouter();
    const logoutMutation = useLogout();

    if (error) {
        console.error(error);
        return (
            <div className={s.userCard}>Network error</div>
        )
    }

    if (isLoading) {
        return (
            <div className={s.userCard}>
                <SkeletonLoader width={40} className={s.skeletonLoader} type="circle"/>
                <SkeletonLoader width={40} className={s.skeletonLoader} type="text"/>
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
                <Button onClick={onLoginClick}>
                    Log in
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
            <span className={s.divider}></span>
            <Button onClick={onLogoutClick}>
                Log out
            </Button>
        </div>
    )
}
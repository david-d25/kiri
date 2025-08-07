import {useBootstrap} from "@/hooks/bootstrap";
import {withSSRQueries} from "@/lib/withSSRQueries";
import {fetchBootstrap} from "@/lib/api/bootstrap";

import styles from './login.module.scss'
import Loading from "../../components/Loading/Loading";
import LoadingError from "../../components/LoadingError/LoadingError";
import TelegramLoginButton from "../../components/TelegramLoginButton/TelegramLoginButton";
import Button from "../../components/Button/Button";
import UserAvatar from "../../components/UserAvatar/UserAvatar";
import {useRouter} from "next/router";
import LoadingOverlay from "../../components/LoadingOverlay/LoadingOverlay";
import {useLogout} from "@/hooks/logout";
import InfoPanel from "../../components/InfoPanel/InfoPanel";

export default function LoginPage() {
    const { data, error, isLoading, isFetching } = useBootstrap()

    if (error != null) {
        return (
            <div className={styles.container}>
                <LoadingError />
            </div>
        );
    }

    if (isLoading || isFetching || !data) {
        return (
            <div className={styles.container}>
                <Loading />
            </div>
        );
    }

    if (data!.isAuthenticated) {
        const user = data!.user!;
        const name = user.firstName + (user.lastName? ` ${user.lastName}` : '');
        return <LoggedIn photoUrl={user.photoUrl} name={name} />;
    } else {
        const {telegramBotUsername, telegramAuthCallbackUrl} = data!.login!;
        return (
            <div className={styles.container}>
                <TelegramLoginButton botUsername={telegramBotUsername} callbackUrl={telegramAuthCallbackUrl}/>
            </div>
        );
    }
}

type LoggedInProps = {
    photoUrl: string | undefined;
    name: string;
}

function LoggedIn(props: LoggedInProps) {
    const router = useRouter();
    const logoutMutation = useLogout();

    const onHomePage = () => router.push('/').then(() => {});

    const onLogoutClick = () => {
        logoutMutation.mutate(undefined, { onSuccess: router.reload });
    };

    const logoutLoading = logoutMutation.isPending

    return (
        <div className={styles.container}>
            <div className={styles.loggedIn}>
                <div className={styles.avatarContainer}>
                    <UserAvatar
                        src={props.photoUrl}
                        name={props.name}
                        size="large"
                    />
                </div>

                <h1 className={styles.title}>
                    You're logged in as <span className={styles.username}>{props.name}</span>
                </h1>

                <div className={styles.actions}>
                    <Button onClick={onHomePage}>Home page</Button>
                    <LoadingOverlay loading={logoutLoading}>
                        <Button onClick={onLogoutClick} disabled={logoutLoading}>Log out</Button>
                    </LoadingOverlay>
                </div>

                { logoutMutation.isError && (
                    <InfoPanel type='error'>Network error: {logoutMutation.error.message}</InfoPanel>
                )}
            </div>
        </div>
    );
}

export const getServerSideProps = withSSRQueries([
    [['bootstrap'], fetchBootstrap],
]);
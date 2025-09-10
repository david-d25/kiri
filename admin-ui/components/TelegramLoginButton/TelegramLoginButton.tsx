import {useEffect, useRef} from 'react';
import Button from "../Button/Button";
import {useFetchDevAuthHash} from "@/hooks/devAuth";
import InfoPanel from "../InfoPanel/InfoPanel";

type Props = {
    botUsername: string;
    callbackUrl: string;
};

export default function TelegramLoginButton(props: Props) {
    const containerRef = useRef<HTMLDivElement>(null);

    if (process.env.NODE_ENV === 'development') {
        return (
            <div style={{ display: 'flex', gap: '10px' }}>
                <TelegramDevLoginButton botUsername={props.botUsername} callbackUrl={props.callbackUrl} userId={1} />
                <TelegramDevLoginButton botUsername={props.botUsername} callbackUrl={props.callbackUrl} userId={2} />
                <TelegramDevLoginButton botUsername={props.botUsername} callbackUrl={props.callbackUrl} userId={3} />
            </div>
        )
    }

    const { botUsername, callbackUrl } = props;

    useEffect(() => {
        const script = document.createElement('script');
        script.src = 'https://telegram.org/js/telegram-widget.js?22';
        script.async = true;
        script.setAttribute('data-telegram-login', botUsername);
        script.setAttribute('data-size', 'large');
        script.setAttribute('data-auth-url', callbackUrl);
        // if needed to request access to send messages
        // script.setAttribute('data-request-access', 'write');

        containerRef.current?.appendChild(script);
    }, [botUsername, callbackUrl]);

    return (
        <div ref={containerRef}>
            {/* button will be put here */}
        </div>
    );
}

function TelegramDevLoginButton(props: Props & { userId: number }) {
    const firstName = 'Test';
    const lastName = 'ID' + props.userId;
    const username = 'test_user' + props.userId;
    const photoUrl = '';
    const authDate = new Date();

    const { data, error, isLoading, isFetching } = useFetchDevAuthHash(
        props.userId,
        firstName,
        lastName,
        username,
        photoUrl,
        authDate
    );

    const buttonEnabled = !isLoading && !isFetching && data != null && !error;

    if (isLoading || isFetching) {
        return <Button disabled>Loading...</Button>
    }

    function onClick() {
        if (!data) {
            return;
        }
        const authDateTimestamp = Math.floor(authDate.getTime() / 1000);
        const params = new URLSearchParams({
            id: props.userId.toString(),
            first_name: encodeURIComponent(firstName),
            last_name: encodeURIComponent(lastName),
            username: encodeURIComponent(username),
            photo_url: encodeURIComponent(photoUrl),
            auth_date: authDateTimestamp.toString(),
            hash: data
        });
        window.location.href = props.callbackUrl + '?' + params.toString();
    }

    return (
        <>
            <Button onClick={onClick} disabled={!buttonEnabled}>DEV Telegram Login, id {props.userId}</Button>
            { error &&
                <InfoPanel type='error'>Failed to fetch hash: {error.message}</InfoPanel>
            }
        </>
    )
}
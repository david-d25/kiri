import { GetServerSideProps } from 'next';

export const getServerSideProps: GetServerSideProps = async () => {
    return {
        redirect: {
            destination: '/engine',
            permanent: false,
        },
    };
};

export default function Home() {
    return null;
}
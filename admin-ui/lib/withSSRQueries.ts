import {dehydrate, QueryClient} from '@tanstack/react-query'
import type {GetServerSideProps} from 'next'

type QueryTuple = [key: readonly unknown[], fn: () => Promise<unknown>]

export function withSSRQueries(
    queries: QueryTuple[],
): GetServerSideProps<{ dehydratedState: unknown }> {
    return async () => {
        const qc = new QueryClient()

        await Promise.all(
            queries.map(([key, fn]) => qc.prefetchQuery({
                queryKey: key,
                queryFn: fn,
            })),
        )

        return {
            props: {
                dehydratedState: dehydrate(qc),
            },
        }
    }
}
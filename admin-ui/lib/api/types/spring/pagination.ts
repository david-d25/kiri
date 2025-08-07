export type PageableDto<T> = {
    content: T;
    pageable: {
        pageNumber: number;
        pageSize: number;
        sort: {
            empty: boolean;
            sorted: boolean;
            unsorted: boolean;
        };
        offset: number;
        pages: boolean;
        unpaged: boolean;
    };
    last: boolean;
    totalPages: number;
    totalElements: number;
    size: number;
    number: number;
    sort: {
        empty: boolean;
        sorted: boolean;
        unsorted: boolean;
    };
    first: boolean;
    numberOfElements: number;
    empty: boolean;
}

export type PaginationParams = {
    page?: number;
    size?: number;
    sort?: string | Array<PaginationSort>;
};

export type PaginationSort = {
    property: string;
    direction: SortDirection;
}

export type SortDirection = 'asc' | 'desc';

export function addPagination(urlSearchParams: URLSearchParams, params: PaginationParams) {
    if (params.page !== undefined) {
        urlSearchParams.set('page', params.page.toString());
    }
    if (params.size !== undefined) {
        urlSearchParams.set('size', params.size.toString());
    }
    if (params.sort) {
        if (Array.isArray(params.sort)) {
            urlSearchParams.set('sort', params.sort.map(s => `${s.property},${s.direction}`).join(','));
        } else {
            urlSearchParams.set('sort', params.sort);
        }
    }
}
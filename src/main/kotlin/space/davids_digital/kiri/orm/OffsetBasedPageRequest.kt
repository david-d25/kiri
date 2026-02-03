package space.davids_digital.kiri.orm

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

class OffsetBasedPageRequest(
    private val offset: Long,
    private val limit: Int,
    private val sort: Sort = Sort.unsorted()
) : Pageable {

    init {
        require(offset >= 0) { "offset must be >= 0" }
        require(limit > 0) { "limit must be > 0" }
    }

    /**
     * The result may be not accurate if the offset is not a multiple of the limit.
     */
    override fun getPageNumber(): Int = (offset / limit).toInt()
    override fun getPageSize(): Int = limit
    override fun getOffset(): Long = offset
    override fun getSort(): Sort = sort

    override fun next(): Pageable = OffsetBasedPageRequest(offset + limit, limit, sort)

    override fun previousOrFirst(): Pageable =
        if (hasPrevious()) OffsetBasedPageRequest((offset - limit).coerceAtLeast(0), limit, sort) else first()

    override fun first(): Pageable = OffsetBasedPageRequest(0, limit, sort)

    override fun withPage(pageNumber: Int): Pageable =
        OffsetBasedPageRequest(pageNumber.toLong() * limit, limit, sort)

    override fun hasPrevious(): Boolean = offset > 0
}
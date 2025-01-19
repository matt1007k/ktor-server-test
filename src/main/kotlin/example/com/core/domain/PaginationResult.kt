package example.com.core.domain

import kotlinx.serialization.Serializable

@Serializable
data class PaginatedResult<T>(
    val data: List<T>,
    val metadata: Metadata
)
@Serializable
data class Metadata(
    val total: Int,
    val lastPage: Int,
    val perPage: Int
)
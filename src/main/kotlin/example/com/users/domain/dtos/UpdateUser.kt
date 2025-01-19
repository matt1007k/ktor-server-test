package example.com.users.domain.dtos

import example.com.users.domain.models.Status

data class UpdateUser(
    val fullName: String?,
    val email: String?,
    val password: String?,
    val avatarUrl: String?,
    val role: Role?,
    val documentType: DocumentType?,
    val documentNumber: String?,
    val status: Status?
)

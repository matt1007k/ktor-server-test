package example.com.users.domain.dtos

import kotlinx.serialization.Serializable

@Serializable
data class CreateUser(
    val fullName: String,
    val email: String,
    val password: String,
    val avatarUrl: String,
    val role: Role,
    val documentType: DocumentType,
    val documentNumber: String
)

enum class Role {
    SELLER,
    ADMIN,
    CUSTOMER
}

enum class DocumentType {
    DNI,
    RUC
}
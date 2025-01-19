package example.com.users.domain.dtos

import kotlinx.serialization.Serializable

@Serializable
data class CreateUser(
    val fullName: String,
    val email: String,
    val password: String,
    val avatarUrl: String,
    val role: Role = Role.SELLER,
    val documentType: DocumentType = DocumentType.DNI,
    val documentNumber: String
)

enum class Role(val value: String) {
    SELLER("SELLER"),
    ADMIN("ADMIN"),
    COSTUMER("COSTUMER"),
}

enum class DocumentType(val value: String) {
    DNI("DNI"),
    RUC("RUC")
}
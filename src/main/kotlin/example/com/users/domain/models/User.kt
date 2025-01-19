package example.com.users.domain.models

import example.com.users.domain.dtos.DocumentType
import example.com.users.domain.dtos.Role
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.EntityID
import org.joda.time.DateTime
import java.util.UUID

@Serializable
data class User(
    val id: String,
    val email: String,
    val fullName: String,
    val avatarUrl: String?,
    val documentType: DocumentType,
    val documentNumber: String,
    val role: Role,
    val status: Status,
    val createdAt: String,
    val updatedAt: String,
)

enum class Status(val value: String) {
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE")
}
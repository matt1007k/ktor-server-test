package example.com.users.data.domain

import example.com.users.domain.dtos.DocumentType
import example.com.users.domain.dtos.Role
import example.com.users.domain.models.Status
import example.com.users.domain.models.User
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.jodatime.datetime
import org.joda.time.DateTime

object UsersTable : UUIDTable("users") {
    val fullName = varchar("fullName", 100)
    val email = varchar("email", 50)
    val password = varchar("password", 100)
    val avatarUrl = varchar("avatarUrl", 100).nullable()
    val documentType =
        enumerationByName("documentType", 50, DocumentType::class).default(DocumentType.DNI)
    val documentNumber = varchar("documentNumber", 11)
    val role = enumerationByName("role", 50, Role::class).default(Role.SELLER)
    val status = enumerationByName("status", 50, Status::class).default(Status.ACTIVE)
    val createdAt = datetime("createdAt").default(DateTime.now())
    val updatedAt = datetime("updatedAt").default(DateTime.now())
}

fun toUserModel(row: ResultRow) = User(
    id = row[UsersTable.id].toString(),
    fullName = row[UsersTable.fullName],
    email = row[UsersTable.email],
    avatarUrl = row[UsersTable.avatarUrl],
    documentType = row[UsersTable.documentType],
    documentNumber = row[UsersTable.documentNumber],
    role = row[UsersTable.role],
    status = row[UsersTable.status],
    createdAt = row[UsersTable.createdAt].toString(),
    updatedAt = row[UsersTable.updatedAt].toString()
)
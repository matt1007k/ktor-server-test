package example.com.users.data.domain.models

import example.com.users.data.domain.models.UsersTable.customEnumeration
import example.com.users.data.domain.models.UsersTable.default
import example.com.users.domain.dtos.DocumentType
import example.com.users.domain.dtos.Role
import example.com.users.domain.models.Status
import example.com.users.domain.models.User
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.jodatime.datetime
import org.joda.time.DateTime
import org.postgresql.util.PGobject
import java.util.UUID

object UsersTable : UUIDTable("users") {
    val fullName = varchar("fullName", 100)
    val email = varchar("email", 50)
    val password = varchar("password", 100)
    val avatarUrl = varchar("avatarUrl", 100).nullable()
    val documentType = enumColumnCustom<DocumentType>(
        column = "documentType",
        enumTypeDb = "DocumentType",
        defaultValue = DocumentType.DNI
    )
    val documentNumber = varchar("documentNumber", 11)
    val role = enumColumnCustom<Role>(
        column = "role",
        enumTypeDb = "Role",
        defaultValue = Role.CUSTOMER
    )
    val status = enumColumnCustom<Status>(
        column = "status",
        enumTypeDb = "Status",
        defaultValue = Status.ACTIVE
    )
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


class PGEnum<T : Enum<T>>(enumTypeName: String, enumValue: T?) : PGobject() {
    init {
        type = enumTypeName
        value = enumValue?.name
    }
}

fun escapeEnum(enumName: String): String = "\"$enumName\""

inline fun <reified T : Enum<T>> enumColumnCustom(
    column: String,
    enumTypeDb: String,
    defaultValue: T
) =
    customEnumeration(
        name = column,
        fromDb = { value -> enumValueOf<T>(value as String) },
        toDb = { PGEnum(escapeEnum(enumTypeDb), it) },
        sql = escapeEnum(enumTypeDb)
    ).default(defaultValue)

//
//class User(id: org.jetbrains.exposed.dao.id.EntityID<UUID>) : UUIDEntity(id) {
//    companion object : UUIDEntityClass<User>(table = UsersTable)
//    var fullName by UsersTable.fullName
////    val roles by Role via UserRoles
//    // Other properties
//}
//
//object Roles : UUIDTable("roles") {
//    val name = varchar("name", 255).uniqueIndex()
//}
//
//class Role(id: org.jetbrains.exposed.dao.id.EntityID<UUID>) : UUIDEntity(id) {
//    companion object : UUIDEntityClass<Role>(Roles)
//    var name by Roles.name
////    val users by User via UserRoles
//    // Other properties
//}
//
//object UserRoles : org.jetbrains.exposed.sql.Table("user_roles") {
//    val user = reference("user_id", UsersTable)
//    val role = reference("role_id", Roles)
//    override val primaryKey = PrimaryKey(user, role) // Composite primary key
//}
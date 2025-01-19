package example.com.users.data.repositories

import example.com.core.data.models.suspendTransaction
import example.com.core.domain.Metadata
import example.com.core.domain.PaginatedResult
import example.com.core.domain.Result
import example.com.users.data.domain.UsersTable
import example.com.users.data.domain.toUserModel
import example.com.users.domain.dtos.CreateUser
import example.com.users.domain.dtos.UpdateUser
import example.com.users.domain.models.User
import example.com.users.domain.repositories.UserRepository
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.util.UUID

class UserRepositoryImpl : UserRepository {
    @Throws(Exception::class)
    override suspend fun getAll(term: String?, page: Int, perPage: Int): Result<PaginatedResult<User>> =
        suspendTransaction {
            try {
                addLogger(StdOutSqlLogger)
                val offset = (page - 1) * perPage
                val query = UsersTable.selectAll()
                term?.let {
                    query.andWhere { (UsersTable.fullName like "%$it%") or (UsersTable.email like "%$it%") }
                }

                val data = query
                    .limit(perPage, offset = offset.toLong())
                    .map(::toUserModel)

//        val total = query.count()
                val total = UsersTable.selectAll().count()
                val lastPage = if (perPage > 0) {
                    Math.ceil(total.toDouble() / perPage).toInt()
                } else {
                    1
                }

                println("total: $total")
                println("users: $data")

                Result.Success(PaginatedResult(
                    data = data,
                    metadata = Metadata(total = total.toInt(), lastPage = lastPage, perPage = perPage)
                ))
            } catch (e: Exception) {
                print("ERROR getAll UserRepositoryImpl: ${e.message.toString()}")
                Result.Error("Error getAll UserRepositoryImpl")
            }

        }

    override suspend fun create(formData: CreateUser): User = suspendTransaction {
        val userId = UsersTable.insert {
            it[fullName] = formData.fullName
            it[email] = formData.email
            it[password] = formData.password
            it[avatarUrl] = formData.avatarUrl
            it[documentType] = formData.documentType
            it[documentNumber] = formData.documentNumber
            it[role] = formData.role
        } get UsersTable.id

        UsersTable.selectAll().andWhere { UsersTable.id eq userId }.first().let { toUserModel(it) }
    }

    override suspend fun findById(id: UUID): User? = suspendTransaction {
        UsersTable.select { UsersTable.id eq id }.map { toUserModel(it) }.singleOrNull()
    }

    override suspend fun findByEmail(email: String): User? = suspendTransaction {
        UsersTable.select { UsersTable.email eq email }.map { toUserModel(it) }.singleOrNull()
    }

    override suspend fun update(id: UUID, formData: UpdateUser): User = suspendTransaction {
        UsersTable.update({ UsersTable.id eq id }) {
            formData.fullName?.let { it1 -> it[fullName] = it1 }
            formData.email?.let { it1 -> it[email] = it1 }
            formData.password?.let { it1 -> it[password] = it1 }
            formData.avatarUrl?.let { it1 -> it[avatarUrl] = it1 }
            formData.documentType?.let { it1 -> it[documentType] = it1 }
            formData.documentNumber?.let { it1 -> it[documentNumber] = it1 }
            formData.role?.let { it1 -> it[role] = it1 }
            formData.status?.let { it1 -> it[status] = it1 }
        }
        UsersTable.select { UsersTable.id eq id }.map { toUserModel(it) }.first()
    }

    override suspend fun delete(id: UUID): User? = suspendTransaction {
        UsersTable.deleteWhere { UsersTable.id eq id }
        UsersTable.select { UsersTable.id eq id }.map { toUserModel(it) }.first()
    }
}
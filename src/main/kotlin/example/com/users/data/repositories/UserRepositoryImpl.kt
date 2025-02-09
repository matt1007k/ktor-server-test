package example.com.users.data.repositories

import example.com.core.data.models.suspendTransaction
import example.com.core.domain.Metadata
import example.com.core.domain.PaginatedResult
import example.com.core.domain.Result
import example.com.users.data.domain.models.UsersTable
import example.com.users.data.domain.models.toUserModel
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
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.util.UUID

class UserRepositoryImpl : UserRepository {
    @Throws(Exception::class)
    override suspend fun getAll(
        term: String?,
        page: Int,
        perPage: Int
    ): Result<PaginatedResult<User>> =
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

                Result.Success(
                    PaginatedResult(
                        data = data,
                        metadata = Metadata(
                            total = total.toInt(),
                            lastPage = lastPage,
                            perPage = perPage
                        )
                    )
                )
            } catch (e: Exception) {
                print("ERROR getAll UserRepositoryImpl: ${e.message.toString()}")
                Result.Error("Error getAll UserRepositoryImpl")
            }

        }

    override suspend fun create(formData: CreateUser): Result<User> = suspendTransaction {
        try {
            val user = UsersTable.insert {
                it[fullName] = formData.fullName
                it[email] = formData.email
                it[password] = formData.password
                it[avatarUrl] = formData.avatarUrl
                it[documentType] = formData.documentType
                it[documentNumber] = formData.documentNumber
                it[role] = formData.role
            }.resultedValues!!
                .first()
                .let { toUserModel(it) }

            Result.Success(user)
        } catch (e: Exception) {
            print("ERROR create UserRepositoryImpl: ${e.message.toString()}")
            Result.Error("Error create UserRepositoryImpl")
        }
    }

    override suspend fun findById(id: String): Result<User?> = suspendTransaction {
        try {
            val uuId = UUID.fromString(id)
            val user = UsersTable.select { UsersTable.id eq uuId }.map { toUserModel(it) }
                .singleOrNull()
            println("findById UserRepositoryImpl: $user")

            Result.Success(user)
        } catch (e: Exception) {
            print("ERROR findById UserRepositoryImpl: ${e.message.toString()}")
            Result.Error("Error findById UserRepositoryImpl")
        }
    }

    override suspend fun findByEmail(email: String): Result<User?> = suspendTransaction {
        try {
            Result.Success(UsersTable.select { UsersTable.email eq email }.map { toUserModel(it) }.singleOrNull())
        } catch (e: Exception) {
            print("ERROR findByEmail UserRepositoryImpl: ${e.message.toString()}")
            Result.Error("Error findByEmail UserRepositoryImpl")
        }
    }

    override suspend fun update(id: String, formData: UpdateUser): Result<User> = suspendTransaction {
        try {
            val uuId = UUID.fromString(id)
            UsersTable.update({ UsersTable.id eq uuId }) {
                formData.fullName?.let { it1 -> it[fullName] = it1 }
                formData.email?.let { it1 -> it[email] = it1 }
                formData.password?.let { it1 -> it[password] = it1 }
                formData.avatarUrl?.let { it1 -> it[avatarUrl] = it1 }
                formData.documentType?.let { it1 -> it[documentType] = it1 }
                formData.documentNumber?.let { it1 -> it[documentNumber] = it1 }
                formData.role?.let { it1 -> it[role] = it1 }
                formData.status?.let { it1 -> it[status] = it1 }
            }

            Result.Success(UsersTable.select { UsersTable.id eq uuId }.map { toUserModel(it) }.first())
        } catch (e: Exception) {
            print("ERROR update UserRepositoryImpl: ${e.message.toString()}")
            Result.Error("Error update UserRepositoryImpl")
        }

    }

    override suspend fun delete(id: String): Result<User?> = suspendTransaction {
        val uuId = UUID.fromString(id)
        try {
            UsersTable.deleteWhere { UsersTable.id eq uuId }
            Result.Success(UsersTable.select { UsersTable.id eq uuId }.map { toUserModel(it) }.first())
        } catch (e: Exception) {
            print("ERROR delete UserRepositoryImpl: ${e.message.toString()}")
            Result.Error("Error delete UserRepositoryImpl")
        }

    }
}
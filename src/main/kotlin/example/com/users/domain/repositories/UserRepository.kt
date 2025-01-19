package example.com.users.domain.repositories

import example.com.core.domain.PaginatedResult
import example.com.core.domain.Result
import example.com.users.domain.dtos.CreateUser
import example.com.users.domain.dtos.UpdateUser
import example.com.users.domain.models.User
import java.util.UUID

interface UserRepository {
    suspend fun getAll(term: String?, page: Int, perPage: Int): Result<PaginatedResult<User>>
    suspend fun create(formData: CreateUser): User
    suspend fun findById(id: UUID): User?
    suspend fun findByEmail(email: String): User?
    suspend fun update(id: UUID, formData: UpdateUser): User
    suspend fun delete(id: UUID): User?
}
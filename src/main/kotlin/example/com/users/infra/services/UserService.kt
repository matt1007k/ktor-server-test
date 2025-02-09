package example.com.users.infra.services

import example.com.users.data.domain.models.UsersTable
import example.com.users.domain.dtos.CreateUser
import example.com.users.domain.dtos.UpdateUser
import example.com.users.domain.models.User
import example.com.users.domain.repositories.UserRepository
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

class UserService(private val userRepository: UserRepository) {

     suspend  fun getAll(term: String?, page: Int, perPage: Int) = userRepository.getAll(term, page, perPage)

     suspend fun create(formData: CreateUser): User {

        if (userRepository.findByEmail(formData.email).getSuccessData() != null) {
            throw Exception("User already exists")
        }

         val salt = BCrypt.gensalt()
         val password = BCrypt.hashpw(formData.password, salt)

        return userRepository.create(
            formData = CreateUser(
                fullName = formData.fullName,
                email = formData.email,
                password = password,
                avatarUrl = formData.avatarUrl,
                documentType = formData.documentType,
                documentNumber = formData.documentNumber,
                role = formData.role
            )
        ).getSuccessData()
    }

    suspend  fun getOne(id: String): User? {
        val user = userRepository.findById(id).getSuccessData() ?: throw Exception("User not found")
        return user
    }

    suspend fun update(id: String, formData: UpdateUser): User {
        userRepository.findById(id).getSuccessData() ?: throw Exception("User not found")
        return userRepository.update(id, formData).getSuccessData()
    }

    suspend fun deleteOne(id: String): User {
        userRepository.findById(id).getSuccessData() ?: throw Exception("User not found")
        return userRepository.delete(id).getSuccessData()!!
    }

    suspend  fun login(email: String, password: String): User {
        val user = userRepository.findByEmail(email).getSuccessData() ?: throw Exception("User not found")
//        if (!BCrypt.checkpw(password, user.password)) {
//            throw Exception("Invalid password")
//        }
        return user
    }
}
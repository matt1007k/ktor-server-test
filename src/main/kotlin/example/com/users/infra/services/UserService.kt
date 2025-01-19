package example.com.users.infra.services

import example.com.users.domain.dtos.CreateUser
import example.com.users.domain.dtos.UpdateUser
import example.com.users.domain.models.User
import example.com.users.domain.repositories.UserRepository
import java.util.UUID

class UserService(private val userRepository: UserRepository) {
     suspend  fun getAll(term: String?, page: Int, perPage: Int) = userRepository.getAll(term, page, perPage)

     suspend fun create(formData: CreateUser): User {

        if (userRepository.findByEmail(formData.email) != null) {
            throw Exception("User already exists")
        }

         val password = formData.password
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
        )
    }

    suspend  fun getOne(id: UUID): User? {
        val user = userRepository.findById(id)
        if(user == null) {
            throw Exception("User not found")
        }
        return user
    }

    suspend fun update(id: UUID, formData: UpdateUser): User {
        userRepository.findById(id) ?: throw Exception("User not found")
        return userRepository.update(id, formData)
    }


    suspend fun deleteOne(id: UUID): User {
        userRepository.findById(id) ?: throw Exception("User not found")
        return userRepository.delete(id)!!
    }
}
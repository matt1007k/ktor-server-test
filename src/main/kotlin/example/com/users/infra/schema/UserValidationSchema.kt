package example.com.users.infra.schema

import example.com.users.domain.dtos.CreateUser
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.requestvalidation.RequestValidation
import io.ktor.server.plugins.requestvalidation.ValidationResult

fun Application.configureRequestValidation() {
    install(RequestValidation) {
        validate<CreateUser> { createUser ->
            val errors = mutableListOf<String>()

            if (createUser.fullName.isBlank()) {
                errors.add("Full name is required.")
            }
            if (createUser.email.isBlank()) {
                errors.add("Email is required.")
            }
            if (createUser.password.isBlank()) {
                errors.add("Password is required.")
            }

            if (errors.isEmpty()) ValidationResult.Valid
            else ValidationResult.Invalid(errors.joinToString("; "))
        }
    }
}
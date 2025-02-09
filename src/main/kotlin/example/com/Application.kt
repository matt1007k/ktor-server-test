package example.com

import example.com.plugins.configureDatabases
import example.com.plugins.configureHTTP
import example.com.plugins.configureRouting
import example.com.plugins.configureSerialization
import example.com.plugins.connectToPostgres
import example.com.users.data.domain.models.UsersTable
import example.com.users.domain.dtos.CreateUser
import example.com.users.infra.route.usersRouting
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.requestvalidation.RequestValidation
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import io.ktor.server.plugins.requestvalidation.ValidationResult
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun main(args: Array<String>) {
    EngineMain.main(args)
}

@Serializable
data class ErrorResponse(val errors: List<ErrorData>)

@Serializable
data class ErrorData(val field: String, val message: String)

fun Application.module() {
    connectToPostgres()
    configureDatabases()
    configureSerialization()

    install(RequestValidation) {
        validate<CreateUser> { createUser ->
            val errors = mutableListOf<String>()

            if (createUser.fullName.isNullOrEmpty()) {
                errors.add("fullName,Full name is required.")
            }
            if (createUser.email.isNullOrEmpty()) {
                errors.add("email,Email is required.")
            }
            if (createUser.password.isNullOrEmpty()) {
                errors.add("password,Password is required.")
            }
            if (createUser.documentNumber.isNullOrEmpty()) {
                errors.add("documentNumber,DocumentNumber is required.")
            }

            if (errors.isEmpty()) ValidationResult.Valid
            else ValidationResult.Invalid(errors)
        }
    }
    install(StatusPages) {
        exception<RequestValidationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(errors = cause.reasons.map {
                    ErrorData(
                        field = it.split(',')[0],
                        message = it.split(',')[1]
                    )
                })
            )
        }
    }
    configureHTTP()
    configureRouting()
    usersRouting()
}

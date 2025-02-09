package example.com.users.infra.route

import example.com.users.data.domain.models.UsersTable
import example.com.users.data.repositories.UserRepositoryImpl
import example.com.users.domain.dtos.CreateUser
import example.com.users.domain.dtos.UpdateUser
import example.com.users.infra.schema.configureRequestValidation
import example.com.users.infra.services.UserService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.usersRouting() {

    val userService = UserService(UserRepositoryImpl())

//    configureRequestValidation()
    routing {
        route("/users") {

            get {
                val page = call.request.queryParameters["page"]?.toInt() ?: 1
                val perPage = call.request.queryParameters["perPage"]?.toInt() ?: 10
                val term = call.request.queryParameters["term"]
                try {
                    val users = userService.getAll(term, page, perPage)
                    call.respond(HttpStatusCode.OK, users.getSuccessData())
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, message = e.message.toString())
                }

            }

            post {
                val userBody = call.receive<CreateUser>()
                try {
                    val user = userService.create(userBody)
                    call.respond(HttpStatusCode.Created, user)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, message = e.message.toString())
                }

            }

            get("/{id}") {
                val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
                try {
                    val user = userService.getOne(id)
                    call.respond(HttpStatusCode.OK, user!!)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            put("/{id}") {
                val id = call.parameters["id"]?.toString()
                    ?: throw IllegalArgumentException("Invalid ID")
                val userBody = call.receive<UpdateUser>()
                try {
                    val user = userService.update(id, userBody)
                    call.respond(HttpStatusCode.OK, user)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, message = e.message.toString())
                }
            }

            delete("/{id}") {
                val id = call.parameters["id"]?.toString()
                    ?: throw IllegalArgumentException("Invalid ID")
                try {
                    val user = userService.deleteOne(id)
                    call.respond(HttpStatusCode.NoContent, user)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, message = e.message.toString())
                }
            }
        }

    }
}

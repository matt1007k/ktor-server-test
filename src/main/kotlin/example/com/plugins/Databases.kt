package example.com.plugins

import example.com.core.data.util.createOrUpdateEnum
import example.com.users.data.domain.models.UsersTable
import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabases() {
    createOrUpdateEnum("OrderStatus", listOf("INITIAL", "PAID"))
    transaction {
        SchemaUtils.create(UsersTable)
    }
}

fun Application.connectToPostgres() {
    Database.connect(
        url = environment.config.property("postgres.url").getString(),
        driver = environment.config.property("postgres.driver").getString(),
        user = environment.config.property("postgres.user").getString(),
        password = environment.config.property("postgres.password").getString()
    )
}

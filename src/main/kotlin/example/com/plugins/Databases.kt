package example.com.plugins

import example.com.core.data.util.createOrUpdateEnum
import example.com.users.data.domain.models.UsersTable
import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.connectToPostgres() {
    Database.connect(
        url = environment.config.property("postgres.url").getString(),
        driver = environment.config.property("postgres.driver").getString(),
        user = environment.config.property("postgres.user").getString(),
        password = environment.config.property("postgres.password").getString()
    )
}

fun Application.configureDatabases() {
    createOrUpdateEnum("DocumentType", listOf("RUC", "DNI"))
    createOrUpdateEnum("Role", listOf("ADMIN", "CUSTOMER", "SELLER"))
    createOrUpdateEnum("Status", listOf("ACTIVE", "INACTIVE"))
    transaction {
        SchemaUtils.create(UsersTable)
    }

    transaction {
        SchemaUtils.createMissingTablesAndColumns(UsersTable)
        // Checks existing schema against table objects
        // CREATEs and ALTERs tables and columns if needed
        // Optionally logs any inconsistencies, with suggested statements to fix
    // https://www.youtube.com/watch?v=YOXWnM_8vz8
    }
}


package example.com

import example.com.plugins.configureDatabases
import example.com.plugins.configureHTTP
import example.com.plugins.configureRouting
import example.com.plugins.configureSerialization
import example.com.users.infra.route.usersRouting
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
//    configureDatabases()
    configureHTTP()
    configureRouting()
    usersRouting()
}

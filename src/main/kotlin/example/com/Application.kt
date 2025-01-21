package example.com

import example.com.plugins.configureHTTP
import example.com.plugins.configureRouting
import example.com.plugins.configureSerialization
import example.com.users.infra.route.usersRouting
import io.ktor.server.application.Application

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureHTTP()
    configureRouting()
    usersRouting()
}

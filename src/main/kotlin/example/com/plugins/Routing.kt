package example.com.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.html.respondHtml
import io.ktor.server.http.content.resources
import io.ktor.server.http.content.static
import io.ktor.server.http.content.staticFiles
import io.ktor.server.http.content.staticResources
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World! 2")
        }

        get("/scalar") {
            call.respondHtml(HttpStatusCode.OK) {
                head {
                    title("Scalar API Reference")
                    meta("charset", "UTF-8")
                    meta("viewport", "width=device-width, initial-scale=1")
                }
                body {
                    div { id = "app" }
                    script(src = "https://cdn.jsdelivr.net/npm/@scalar/api-reference") {}
                    script {
                        +"""
                            Scalar.createApiReference('#app', {
                                url: '/openapi.json', // Or a different endpoint that serves your spec
                            });
                        """.trimIndent()
                    }
                }
            }
        }
    }

}

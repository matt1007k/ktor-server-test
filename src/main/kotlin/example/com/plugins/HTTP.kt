package example.com.plugins

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.swagger.codegen.v3.generators.html.StaticHtmlCodegen
import java.io.File

fun Application.configureHTTP() {
    routing {
        openAPI(path = "openapi", swaggerFile = "openapi/documentation.yaml") {
            codegen = StaticHtmlCodegen()
        }
        get("/openapi.json") {
            val yamlFileContent = this::class.java.classLoader.getResource("openapi/documentation.yaml")?.readText() ?: error("data.yaml not found in resources")
            val yamlMapper = ObjectMapper(YAMLFactory())
            val openApiSpecMap = yamlMapper.readValue(yamlFileContent, Map::class.java)
            val jsonMapper = ObjectMapper()
            val jsonString = jsonMapper.writeValueAsString(openApiSpecMap)
            call.respondText(jsonString, io.ktor.http.ContentType.Application.Json)
        }
    }
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader("MyCustomHeader")
        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }
}

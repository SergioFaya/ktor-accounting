package nomad.digital

import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import nomad.digital.infrastructure.storage.exposed.config.DatabaseFactory
import nomad.digital.plugins.*

fun main(args: Array<String>) {
    HoconConfigLoader()
    val env = applicationEngineEnvironment {
        module(Application::module)
        connector {
            host = "0.0.0.0"
            port = 8080
        }
    }
    embeddedServer(Netty, env).start(true)
}

fun Application.module() {
    DatabaseFactory.init()
    configureTemplating()
    configureSerialization()
    configureRouting()
    configurePostgres()
}

package nomad.digital

import io.ktor.server.application.Application
import io.ktor.server.config.HoconConfigLoader
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import nomad.digital.infrastructure.storage.exposed.config.DatabaseFactory
import nomad.digital.plugins.configurePostgres
import nomad.digital.plugins.configureRouting
import nomad.digital.plugins.configureSerialization

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
    configureSerialization()
    configureRouting()
    configurePostgres()
}

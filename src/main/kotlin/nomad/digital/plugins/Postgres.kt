package nomad.digital.plugins

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

fun Application.configurePostgres() {
    Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver", user = "root", password = "")
}

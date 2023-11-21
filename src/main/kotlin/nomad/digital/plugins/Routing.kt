package nomad.digital.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.httpMethod
import io.ktor.server.request.uri
import io.ktor.server.resources.Resources
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.webjars.Webjars
import kotlinx.serialization.json.Json
import nomad.digital.plugins.routers.accountsRouter
import nomad.digital.plugins.routers.defaultErrorResponse

fun Application.configureRouting() {
    install(Webjars) {
        path = "/assets" //defaults to /webjars
    }
    install(Resources)
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                isLenient = true
            }
        )
    }
    install(StatusPages) {
        exception<Throwable> { call: ApplicationCall, cause ->
            call.defaultErrorResponse("Exception ${cause.cause} with message: ${cause.localizedMessage}")
        }

        unhandled { call ->
            call.defaultErrorResponse(
                "Unhandled: ${call.request.uri} ${call.request.httpMethod} " +
                        "- Params: ${call.parameters}"
            )
        }

        status(HttpStatusCode.NotFound, HttpStatusCode.UnsupportedMediaType) { call, status ->
            call.defaultErrorResponse("Status: $status - Params:  ${call.parameters}")
        }

    }
    routing {
        staticResourceRouter()

        get("/") {
            call.respondRedirect("/accounts", permanent = true)
        }

        accountsRouter()
    }
}

fun Route.staticResourceRouter() {
    // Static plugin. Try to access `/static/error-message.html`
    staticResources("/static", "static")
}

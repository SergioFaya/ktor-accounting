package nomad.digital.plugins.routers

import io.ktor.server.application.ApplicationCall
import io.ktor.server.html.respondHtml
import kotlinx.html.a
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.img
import kotlinx.html.role


suspend fun ApplicationCall.defaultErrorResponse(message: String) =

    respondHtml {
        baseTemplate {
            h1 { +"TODO MAL" }

            div("alert alert-danger") {
                role = "alert"
                +"Message: $message"
            }

            img { src = "/static/img/error-message.jpg" }

            a("/") {
                classes = setOf("btn", "btn-primary")
                +"tira pa casa"
            }
        }
    }

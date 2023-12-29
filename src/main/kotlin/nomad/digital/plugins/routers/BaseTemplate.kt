package nomad.digital.plugins.routers

import kotlinx.html.DIV
import kotlinx.html.HTML
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.nav
import kotlinx.html.script
import kotlinx.html.style
import kotlinx.html.title
import javax.management.Query.div

fun HTML.baseTemplate(content: DIV.() -> Unit = {}) {
    head {
        link {
            rel = "stylesheet"
            href = "/assets/bootstrap/bootstrap.css"
        }
        meta { charset = "utf-8" }
        meta { name = "viewport" }
        title { +"Bootstrap demo" }
    }
    body {
        nav("navbar bg-dark border-bottom border-body") {
            attributes["data-bs-theme"] = "dark"

            div(classes = "container-fluid") {
                a(href = "/", classes = "navbar-brand mb-0 h1 text-primary") {
                    +"Anxiety Money Planner"
                }
            }
        }

        div(classes = "container pt-4 px-4") {
            style = "max-width: 1600px;"
            content()
        }

        script {
            src = "https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"
            integrity = "sha384-C6RzsynM9kWDrMNeT87bh95OGNyZPhcTNXj1NW7RuBCsyN/o0jlpcV8Qyq46cDfL"
            attributes["crossorigin"] = "anonymous"
        }
    }
}

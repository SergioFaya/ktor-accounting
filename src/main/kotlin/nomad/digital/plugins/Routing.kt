package nomad.digital.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.html.respondHtml
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.resources.Resources
import io.ktor.server.resources.get
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.webjars.Webjars
import kotlinx.coroutines.runBlocking
import kotlinx.html.DIV
import kotlinx.html.HTML
import kotlinx.html.ThScope
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.button
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.head
import kotlinx.html.img
import kotlinx.html.li
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.p
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.title
import kotlinx.html.tr
import kotlinx.html.ul
import nomad.digital.infrastructure.storage.exposed.findAccount
import nomad.digital.infrastructure.storage.exposed.findAccounts

fun Application.configureRouting() {
    install(Webjars) {
        path = "/assets" //defaults to /webjars
    }
    install(Resources)
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondHtml {

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
                    div(classes = "container mt-4 bg-secondary") {
                        h1 { +"TODO MAL" }

                        div {
                            img { src="/static/img/error-message.jpg" }
                        }

                        a("/") {
                            classes= setOf("btn","btn-primary")
                            +"tira pa casa"
                        }
                    }
                }
            }
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

fun Route.accountsRouter() {

    fun HTML.htmlBase(block: DIV.() -> Unit = {}) {
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
            div(classes = "container mt-4") {
                div(classes = "row") {
                    div(classes = "col-8") {
                        block()
                    }
                    div(classes = "col-4 bg-primary") {
                        h2 { +"Summary" }

                        p { +"Total Savingss" }
                        p { +"Total Investment" }
                        p { +"Total Cassasd asa" }
                    }
                }
            }
        }
    }

    // /root/development/kotlin/ktor-digital-nomad

    get<AccountResource> {
        val accounts = runBlocking {
            findAccounts()
        }
        call.respondHtml {
            htmlBase {
                h1 { +"Your accounts" }
                ul {
                    accounts.forEach {
                        li {
                            val link = "/accounts/${it.id}"
                            a(link) { +"Account ${it.title}" }
                        }
                    }
                }
            }
        }
    }

    get<AccountResource.ById> { accountById ->
        val account = findAccount(id = accountById.id)

        call.respondHtml {
            htmlBase {
                h1 { +"Account information: ${account.title}" }
                table(classes = "table") {
                    thead {
                        tr(classes = "table-dark") {
                            th(scope = ThScope.col) {
                                +"Name"
                            }
                            th(scope = ThScope.col) {
                                +"Date"
                            }
                            th(scope = ThScope.col) {
                                +"Amount"
                            }
                            th(scope = ThScope.col) {
                                +"Category"
                            }
                        }
                    }
                    tbody {
                        account.transactions.forEach {
                            tr {
                                th(scope = ThScope.row) {
                                    +it.name
                                }
                                td {
                                    +"${it.date}"
                                }
                                td {
                                    +"${it.amount}"
                                }
                                td {
                                    +"${it.category}"
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Resource("/accounts")
class AccountResource {

    @Resource("{id}")
    class ById(val parent: AccountResource = AccountResource(), val id: Long)
}

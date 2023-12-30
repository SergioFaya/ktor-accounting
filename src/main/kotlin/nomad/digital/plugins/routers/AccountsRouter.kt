package nomad.digital.plugins.routers

import io.ktor.http.ContentType
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.resources.Resource
import io.ktor.server.application.call
import io.ktor.server.html.respondHtml
import nomad.digital.infrastructure.storage.exposed.deleteTransactions

import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.request.receiveParameters
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Route
import kotlinx.coroutines.runBlocking
import nomad.digital.domain.Account
import nomad.digital.infrastructure.readBankTransactions
import nomad.digital.infrastructure.storage.exposed.addAccount
import nomad.digital.infrastructure.storage.exposed.deleteAccount
import nomad.digital.infrastructure.storage.exposed.findAccount
import nomad.digital.infrastructure.storage.exposed.findAccounts

@Resource("/accounts")
class AccountResource {
    @Resource("new")
    class New(val parent: AccountResource = AccountResource())

    @Resource("{id}")
    class ById(val parent: AccountResource = AccountResource(), val id: Long)

    @Resource("{id}/delete")
    class DeleteById(val parent: AccountResource = AccountResource(), val id: Long)

    @Resource("{id}/transactions")
    class UploadTransactions(val parent: AccountResource = AccountResource(), val id: Long)
}

@Resource("/transactions")
class TransactionsResource {
    @Resource("{id}/delete")
    class DeleteById(val parent: TransactionsResource = TransactionsResource(), val id: Long)
}

fun Route.accountsRouter() {
    get<TransactionsResource.DeleteById> { deleteById ->

        val id = deleteById.id

        deleteTransactions(1, listOf(deleteById.id))

        call.respondRedirect("/accounts/1")
    }

    get<AccountResource> {
        val accounts =
            runBlocking {
                findAccounts()
            }
        call.respondHtml {
            listAccounts(accounts)
        }
    }

    get<AccountResource.ById> { accountById ->
        val account = findAccount(id = accountById.id)

        call.respondHtml {
            listAccount(account)
        }
    }

    /**
     * Sending data
     * https://ktor.io/docs/testing.html#json-data
     * https://ktor.io/docs/testing.html#x-www-form-urlencoded
     * https://ktor.io/docs/testing.html#multipart-form-data
     */
    post<AccountResource.New> {
        val accountName =
            when (val contentType = call.request.headers["Content-Type"]) {
                ContentType.Application.Json.toString() -> call.receive<Account>().accountName

                ContentType.Application.FormUrlEncoded.toString() -> {
                    val formParameters = call.receiveParameters()
                    formParameters[Account::accountName.name].toString()
                }

                else -> {
                    call.defaultErrorResponse("Cannot handle content type $contentType")
                    return@post
                }
            }

        addAccount(Account(accountName = accountName))
        call.respondRedirect("/accounts")
    }

    post<AccountResource.UploadTransactions> { uploadTransactions ->
        val accountId = uploadTransactions.id

        call.receiveMultipart().forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {}
                is PartData.FileItem -> {
                    part.streamProvider().readBankTransactions(accountId)
                }
                else -> {}
            }
            part.dispose()
        }

        call.respondRedirect("/accounts/$accountId")
    }

    get<AccountResource.DeleteById> { deleteById ->
        deleteAccount(deleteById.id)

        call.respondRedirect("/accounts", permanent = true)
    }
}

package nomad.digital.plugins.routers

import io.ktor.http.ContentType
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.resources.Resource
import io.ktor.server.application.call
import io.ktor.server.html.respondHtml
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.request.receiveParameters
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Route
import kotlinx.coroutines.runBlocking
import nomad.digital.domain.Account
import nomad.digital.domain.AccountTransaction
import nomad.digital.domain.TransactionCategory
import nomad.digital.infrastructure.readBankTransactions
import nomad.digital.infrastructure.storage.exposed.addAccount
import nomad.digital.infrastructure.storage.exposed.deleteAccount
import nomad.digital.infrastructure.storage.exposed.deleteTransactions
import nomad.digital.infrastructure.storage.exposed.findAccount
import nomad.digital.infrastructure.storage.exposed.findAccounts
import nomad.digital.infrastructure.storage.exposed.updateTransaction
import java.math.BigDecimal

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

    @Resource("/{id}/transactions/{transactionId}/delete")
    class DeleteTransactionById(val parent: AccountResource = AccountResource(), val id: Long, val transactionId: Long)

    @Resource("/{id}/transactions/{transactionId}")
    class TransactionById(val parent: AccountResource = AccountResource(), val id: Long, val transactionId: Long)
}

fun Route.accountsRouter() {
    post<AccountResource.TransactionById> { updateById ->

        val accountId = updateById.id
        val transactionId = updateById.transactionId

        val category: TransactionCategory =
            when (val contentType = call.request.headers["Content-Type"]) {
                ContentType.Application.Json.toString() -> call.receive<AccountTransaction>().category

                ContentType.Application.FormUrlEncoded.toString() -> {
                    val formParameters = call.receiveParameters()
                    val categoryString = formParameters[AccountTransaction::category.name]
                    if (categoryString == null) {
                        println("invalid input name")
                        return@post
                    }
                    TransactionCategory.valueOf(categoryString)
                }

                else -> {
                    call.defaultErrorResponse("Cannot handle content type $contentType")
                    return@post
                }
            }

        updateTransaction(accountId, transactionId, category)

        call.respondRedirect("/accounts/$accountId")
    }

    get<AccountResource.DeleteTransactionById> { deleteById ->

        val accountId = deleteById.id
        val transactionId = deleteById.transactionId

        deleteTransactions(accountId, listOf(transactionId))

        call.respondRedirect("/accounts/$accountId")
    }

    get<AccountResource> {
        val accounts =
            runBlocking {
                findAccounts()
            }

        val transactions = accounts.flatMap { it.accountTransactions }
        val balance = transactions.fold(BigDecimal(0)) { acc: BigDecimal, element: AccountTransaction -> acc + element.amount }

        val categoriesBalance: Map<TransactionCategory, BigDecimal> =
            transactions.groupingBy { it.category }
                .foldTo(mutableMapOf(), BigDecimal(0)) { acc: BigDecimal, e: AccountTransaction -> acc + e.amount }

        call.respondHtml {
            listAccounts(accounts, balance, categoriesBalance)
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
        println("new account with name $accountName")
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

package nomad.digital.plugins.routers

import kotlinx.html.ButtonType
import kotlinx.html.DIV
import kotlinx.html.FormEncType
import kotlinx.html.FormMethod
import kotlinx.html.HTML
import kotlinx.html.InputType
import kotlinx.html.ThScope
import kotlinx.html.a
import kotlinx.html.button
import kotlinx.html.canvas
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.id
import kotlinx.html.input
import kotlinx.html.label
import kotlinx.html.li
import kotlinx.html.p
import kotlinx.html.script
import kotlinx.html.tabIndex
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr
import kotlinx.html.ul
import kotlinx.html.unsafe
import nomad.digital.domain.Account
import nomad.digital.domain.TransactionCategory
import java.math.BigDecimal

private fun HTML.accountPageBase(
    title: String = "",
    actions: DIV.() -> Unit = {},
    mainContent: DIV.() -> Unit = {},
) = baseTemplate {
    div(classes = "row") {
        div(classes = "col-12") {
            h1 { +title }
        }
    }
    div(classes = "row") {
        div(classes = "col-9") {
            mainContent()
        }
        div(classes = "col-3") {
            div("card bg-warning text-dark bg-opacity-25") {
                div("card-header") { +"Summary" }
                div(classes = "card-body") {
                    p { +"Total Savings" }
                    p { +"Total Investment" }
                    p { +"TODO: add savings performance" }
                }
                div("card-footer d-grid gap-2") {
                    actions()
                }
            }
        }
    }
}

private fun DIV.confirmModal(
    modalId: String = "modal",
    title: String = "title",
    formUrl: String = "/",
    method: FormMethod = FormMethod.get,
    confirmationMessage: String = "Agree to proceed?",
) {
    formModal(
        modalId = modalId,
        title = title,
        formUrl = formUrl,
        method = method,
    ) {
        p { +confirmationMessage }
    }
}

private fun DIV.formModal(
    modalId: String = "modal",
    title: String = "title",
    formUrl: String = "/",
    encoding: FormEncType = FormEncType.applicationXWwwFormUrlEncoded,
    method: FormMethod = FormMethod.post,
    modalBody: DIV.() -> Unit = {},
) {
    button(classes = "btn btn-dark btn-block") {
        type = ButtonType.button
        attributes["data-bs-toggle"] = "modal"
        attributes["data-bs-target"] = "#$modalId"
        +title
    }

    div("modal fade") {
        id = modalId
        tabIndex = "-1"
        attributes["aria-labelledby"] = "${modalId}Label"
        attributes["aria-hidden"] = "true"
        div("modal-dialog") {
            div("modal-content") {
                div("modal-header") {
                    h1("modal-title fs-5") {
                        id = "${modalId}Label"
                        +title
                    }
                    button(classes = "btn-close") {
                        type = ButtonType.button
                        attributes["data-bs-dismiss"] = "modal"
                        attributes["aria-label"] = "Close"
                    }
                }
                form(
                    formUrl,
                    encType = encoding,
                    method = method,
                ) {
                    id = "${modalId}Form"
                    div("modal-body mb-3") {
                        modalBody()
                    }
                    div("modal-footer") {
                        button(classes = "btn btn-primary") {
                            attributes["data-bs-dismiss"] = "modal"
                            type = ButtonType.submit
                            +"Submit"
                        }
                    }
                }
            }
        }
    }
}

private fun DIV.lineChart(
    labels: List<String>,
    values: List<BigDecimal>,
) {
    canvas {
        id = "canvas-id"
    }

    script {
        src = "https://cdn.jsdelivr.net/npm/chart.js@4.0.1/dist/chart.umd.min.js"
        attributes["crossorigin"] = "anonymous"
    }
    script {
        unsafe {
            raw(
                """
                |new Chart(document.getElementById("canvas-id"), {
                |	type : 'line',
                |	data : {
                |		labels : [ ${labels.joinToString(","){"'$it'"} } ],
                |		datasets : [
                |				{
                |					data : [ ${values.joinToString(","){"'$it'"} } ],
                |					label : "Savings",
                |					borderColor : "#3cba9f",
                |					fill: true,
                |					stepped: true
                |				}]
                |	},
                |	options : {
                |		title : {
                |			display : true,
                |			text : 'Savings'
                |		}
                |	}
                |});
                """.trimMargin(),
            )
        }
    }
}

internal fun HTML.listAccount(account: Account) =
    accountPageBase(
        "Account information: ${account.accountName}",
        actions = {
            formModal(
                modalId = "newTransactionModal",
                title = "Add Transactions",
                encoding = FormEncType.multipartFormData,
                formUrl = "/accounts/${account.id}/transactions",
            ) {
                label("form-label") {
                    htmlFor = "accountName"
                    +"Account Transactions"
                }
                input(classes = "form-control") {
                    type = InputType.file
                    id = "accountTransactions"
                    name = "accountTransactions"
                    attributes["aria-describedby"] = "formAccountTransactions"
                }
                div("form-text") {
                    id = "formAccountTransactions"
                    +"Bank Sabadell xlsx format without headers"
                }
            }
        },
    ) {
        val accountsTransactions = account.accountTransactions.groupBy { "${it.date.month}-${it.date.year}" }

        val labels = account.accountTransactions.map { "${it.date.dayOfMonth}-${it.date.month}-${it.date.year}" }

        var accumulator = BigDecimal(0)

        val dataset =
            account.accountTransactions.map { transaction ->
                accumulator = accumulator + transaction.amount
                accumulator
            }

        lineChart(labels = labels, values = dataset)

        div("accordion") {
            id = "accordionPanelsStayOpenExample"

            accountsTransactions.forEach {
                div("accordion-item") {
                    h2("accordion-header") {
                        button(classes = "accordion-button collapsed") {
                            type = ButtonType.button
                            attributes["data-bs-toggle"] = "collapse"
                            attributes["data-bs-target"] = "#${it.key}"
                            attributes["aria-expanded"] = "false"
                            attributes["aria-controls"] = it.key
                            +"${it.key} - Balance: ${it.value.fold(BigDecimal(0)) { acc, value -> acc + value.amount }}"
                        }
                    }
                    div("accordion-collapse collapse") {
                        id = it.key
                        div("accordion-body") {
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
                                        th(scope = ThScope.col) {
                                            +"Actions"
                                        }
                                    }
                                }
                                tbody {
                                    it.value.forEach { transaction ->
                                        tr {
                                            th(scope = ThScope.row) {
                                                +transaction.concept
                                            }
                                            td {
                                                +"${transaction.date}"
                                            }
                                            td {
                                                +"${transaction.amount}"
                                            }
                                            td {
                                                div("dropdown") {
                                                    button(classes = "btn btn-secondary dropdown-toggle") {
                                                        attributes["data-bs-toggle"] = "dropdown"
                                                        attributes["aria-expanded"] = "false"
                                                        +"${transaction.category}"
                                                    }

                                                    ul("dropdown-menu") {
                                                        TransactionCategory.values().forEach { category ->
                                                            li {
                                                                form(
                                                                    "/accounts/${account.id}/transactions/${transaction.id}",
                                                                    encType = FormEncType.applicationXWwwFormUrlEncoded,
                                                                    method = FormMethod.post,
                                                                ) {
                                                                    id = "updateForm"
                                                                    // TODO: make a post to update the category
                                                                    // TODO: fix category listing as it is showint
                                                                    input(classes = "form-control visually-hidden") {
                                                                        type = InputType.text
                                                                        id = "category"
                                                                        name = "category"
                                                                        attributes["aria-describedby"] = "formAccountName"
                                                                        value = "$category"
                                                                    }

                                                                    if (category == transaction.category) {
                                                                        button(classes = "dropdown-item active") {
                                                                            attributes["aria-current"] = "true"
                                                                            type = ButtonType.submit
                                                                            +"$category"
                                                                        }
                                                                    } else {
                                                                        button(classes = "dropdown-item") {
                                                                            type = ButtonType.submit
                                                                            +"$category"
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            td {
                                                div {
                                                    confirmModal(
                                                        modalId = "confirmDeleteModal",
                                                        title = "Delete transaction?",
                                                        formUrl = "/accounts/${account.id}/transactions/${transaction.id}/delete",
                                                        confirmationMessage = "Transaction ${transaction.concept} will be deleted",
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

internal fun HTML.listAccounts(
    accounts: List<Account>,
    balance: BigDecimal,
    categoriesBalance: Map<TransactionCategory, BigDecimal>,
) = accountPageBase(
    "Your accounts",
    actions = {
        formModal(
            modalId = "newAccountModal",
            title = "New Account",
            formUrl = "/accounts/new",
        ) {
            label("form-label") {
                htmlFor = "accountName"
                +"Account Name"
            }
            input(classes = "form-control") {
                type = InputType.text
                id = "accountName"
                name = "accountName"
                attributes["aria-describedby"] = "formAccountName"
            }
            div("form-text") {
                id = "formAccountName"
                +"Name of the account to open"
            }
        }

        button(classes = "btn btn-dark btn-block") {
            type = ButtonType.button
            attributes["data-bs-toggle"] = "modal"
            attributes["data-bs-target"] = "#newAccountModal"
            +"Manage Categories"
        }
    },
) {
    div(classes = "row px-4") {
        ul {
            categoriesBalance.forEach {
                li {
                    +"${it.key}: ${it.value}"
                }
            }
        }
        p { +"Balance: $balance" }
    }

    div(classes = "row px-4") {
        ul {
            accounts.forEach {
                li {
                    val link = "/accounts/${it.id}"

                    div {
                        a(link) { +"Account ${it.accountName}" }
                        +"|"
                        a("$link/delete") {
                            +"Delete ${it.accountName}"
                        }
                    }
                }
            }
        }
    }
}

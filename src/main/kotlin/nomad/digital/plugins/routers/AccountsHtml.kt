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
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.id
import kotlinx.html.input
import kotlinx.html.label
import kotlinx.html.li
import kotlinx.html.p
import kotlinx.html.tabIndex
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr
import kotlinx.html.ul
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

internal fun HTML.listAccount(account: Account) =
    accountPageBase("Account information: ${account.accountName}", actions = {
        button(classes = "btn btn-dark btn-block") {
            type = ButtonType.button
            attributes["data-bs-toggle"] = "modal"
            attributes["data-bs-target"] = "#newTransactionModal"
            +"Add Transactions"
        }
    }) {
        div("modal fade") {
            id = "newTransactionModal"
            tabIndex = "-1"
            attributes["aria-labelledby"] = "newTransactionModalLabel"
            attributes["aria-hidden"] = "true"
            div("modal-dialog") {
                div("modal-content") {
                    div("modal-header") {
                        h1("modal-title fs-5") {
                            id = "newTransactionModalLabel"
                            +"Add Transactions"
                        }
                        button(classes = "btn-close") {
                            type = ButtonType.button
                            attributes["data-bs-dismiss"] = "modal"
                            attributes["aria-label"] = "Close"
                        }
                    }
                    form(
                        "/accounts/${account.id}/transactions",
                        encType = FormEncType.multipartFormData,
                        method = FormMethod.post,
                    ) {
                        id = "transactionFileForm"
                        div("modal-body mb-3") {
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
                                +"Headerless CSV values of the transactions following this format - Operativa,Concepto,F. Valor,Importe,Saldo,Referencia 1,Referencia 2"
                            }
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

        div("accordion") {
            id = "accordionPanelsStayOpenExample"

            val accountsTransactions = account.accountTransactions.groupBy { "${it.date.month}-${it.date.year}" }

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
														+ "${transaction.category}"
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

																	if(category == transaction.category) {
																		button(classes ="dropdown-item active") {
																			attributes["aria-current"] = "true"
																			type = ButtonType.submit
																			+"$category"
																		}
																	}
																	else {
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
                                                a("/accounts/${account.id}/transactions/${transaction.id}/delete") {
                                                    +"Delete x"
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

internal fun HTML.listAccounts(accounts: List<Account>) =
    accountPageBase("Your accounts", actions = {
        button(classes = "btn btn-dark btn-block") {
            type = ButtonType.button
            attributes["data-bs-toggle"] = "modal"
            attributes["data-bs-target"] = "#newAccountModal"
            +"New Account"
        }
    }) {
        div("modal fade") {
            id = "newAccountModal"
            tabIndex = "-1"
            attributes["aria-labelledby"] = "newAccountModalLabel"
            attributes["aria-hidden"] = "true"
            div("modal-dialog") {
                div("modal-content") {
                    div("modal-header") {
                        h1("modal-title fs-5") {
                            id = "newAccountModalLabel"
                            +"Add Account"
                        }
                        button(classes = "btn-close") {
                            type = ButtonType.button
                            attributes["data-bs-dismiss"] = "modal"
                            attributes["aria-label"] = "Close"
                        }
                    }
                    form(
                        "/accounts/new",
                        encType = FormEncType.applicationXWwwFormUrlEncoded,
                        method = FormMethod.post,
                    ) {
                        id = "newAccountForm"
                        div("modal-body mb-3") {
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

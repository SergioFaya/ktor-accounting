package nomad.digital.infrastructure.storage.exposed

import kotlinx.datetime.toKotlinLocalDate
import nomad.digital.domain.Account
import nomad.digital.domain.Transaction
import nomad.digital.infrastructure.storage.exposed.config.DatabaseFactory.database
import nomad.digital.infrastructure.storage.exposed.config.DatabaseFactory.dbQuery
import nomad.digital.infrastructure.storage.exposed.entity.AccountEntity
import nomad.digital.infrastructure.storage.exposed.entity.TransactionEntity
import nomad.digital.infrastructure.storage.exposed.entity.toAccount
import org.jetbrains.exposed.sql.transactions.transaction

suspend fun addTransaction(transaction: Transaction, accountId: Long): Long = dbQuery {
    val account = AccountEntity[accountId]

    TransactionEntity.new {
        this.account = account
        this.amount = transaction.amount
        this.date = transaction.date.toKotlinLocalDate()
        this.name = transaction.name
        this.category = transaction.category.name

    }.id.value
}

suspend fun addAccount(account: Account): Long = dbQuery {
    val accountId = transaction(db = database) {
        AccountEntity.new {
            this.title = account.title
        }.id.value
    }

    val accountEntity = AccountEntity[accountId]

    account.transactions.forEach {

        TransactionEntity.new {
            this.account = accountEntity
            this.amount = it.amount
            this.date = it.date.toKotlinLocalDate()
            this.name = it.name
            this.category = it.category.name

        }.id.value
    }

    accountId
}

suspend fun findAccount(id: Long): Account = dbQuery {
    AccountEntity[id].toAccount()
}

suspend fun findAccounts(): List<Account> = dbQuery {
    AccountEntity.all().map { it.toAccount() }
}
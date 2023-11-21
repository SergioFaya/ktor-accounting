package nomad.digital.infrastructure.storage.exposed

import kotlinx.datetime.toKotlinLocalDate
import nomad.digital.domain.Account
import nomad.digital.domain.AccountTransaction
import nomad.digital.infrastructure.storage.exposed.config.DatabaseFactory.database
import nomad.digital.infrastructure.storage.exposed.config.DatabaseFactory.dbQuery
import nomad.digital.infrastructure.storage.exposed.entity.AccountEntity
import nomad.digital.infrastructure.storage.exposed.entity.AccountTransactionEntity
import nomad.digital.infrastructure.storage.exposed.entity.toAccount
import org.jetbrains.exposed.sql.transactions.transaction

suspend fun addTransaction(accountTransaction: AccountTransaction, accountId: Long): Long = dbQuery {
    val account = AccountEntity[accountId]

    AccountTransactionEntity.new {
        this.account = account
        this.amount = accountTransaction.amount
        this.date = accountTransaction.date
        this.name = accountTransaction.concept
        this.category = accountTransaction.category.name

    }.id.value
}

suspend fun addAccount(account: Account): Long = dbQuery {
    val accountId = transaction(db = database) {
        AccountEntity.new {
            this.title = account.accountName
        }.id.value
    }

    val accountEntity = AccountEntity[accountId]

    account.accountTransactions.forEach {

        AccountTransactionEntity.new {
            this.account = accountEntity
            this.amount = it.amount
            this.date = it.date
            this.name = it.concept
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

suspend fun deleteAccount(id: Long) = dbQuery {
    AccountEntity[id].delete()
}
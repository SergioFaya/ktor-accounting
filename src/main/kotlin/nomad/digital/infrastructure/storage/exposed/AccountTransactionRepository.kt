package nomad.digital.infrastructure.storage.exposed

import nomad.digital.domain.AccountTransaction
import nomad.digital.infrastructure.storage.exposed.config.DatabaseFactory.dbQuery
import nomad.digital.infrastructure.storage.exposed.entity.AccountEntity
import nomad.digital.infrastructure.storage.exposed.entity.AccountTransactionEntity


suspend fun batchInsertAccountTransactions(accountId: Long, accountTransactions: List<AccountTransaction>) = dbQuery {
    accountTransactions.forEach {
        AccountTransactionEntity.new {
            name = it.concept
            amount = it.amount
            date = it.date
            category = it.category.name
            account = AccountEntity[accountId]
        }
    }
}
package nomad.digital.infrastructure.storage.exposed.entity

import nomad.digital.domain.Account
import nomad.digital.infrastructure.storage.exposed.table.AccountTable
import nomad.digital.infrastructure.storage.exposed.table.TransactionTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class AccountEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<AccountEntity>(AccountTable)

    var title by AccountTable.title
    val transactions by TransactionEntity referrersOn TransactionTable.account
}

fun AccountEntity.toAccount() = Account(
    id = id.value,
    title = title,
    transactions = transactions.map(TransactionEntity::toTransaction)
)
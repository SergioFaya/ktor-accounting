package nomad.digital.infrastructure.storage.exposed.entity

import nomad.digital.domain.AccountTransaction
import nomad.digital.domain.TransactionCategory
import nomad.digital.infrastructure.storage.exposed.table.TransactionTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class AccountTransactionEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<AccountTransactionEntity>(TransactionTable)

    var name by TransactionTable.name
    var amount by TransactionTable.amount
    var date by TransactionTable.date
    var category by TransactionTable.category

    var account by AccountEntity referencedOn TransactionTable.account
}

fun AccountTransactionEntity.toTransaction() = AccountTransaction(
    id = id.value,
    concept = name,
    date = date,
    amount = amount,
    category = TransactionCategory.valueOf(category)
)
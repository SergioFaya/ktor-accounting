package nomad.digital.infrastructure.storage.exposed.entity

import kotlinx.datetime.toJavaLocalDate
import nomad.digital.domain.Transaction
import nomad.digital.domain.TransactionCategory
import nomad.digital.infrastructure.storage.exposed.table.TransactionTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class TransactionEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<TransactionEntity>(TransactionTable)

    var name by TransactionTable.name
    var amount by TransactionTable.amount
    var date by TransactionTable.date
    var category by TransactionTable.category

    var account by AccountEntity referencedOn TransactionTable.account
}

fun TransactionEntity.toTransaction() = Transaction(
    id = id.value,
    name = name,
    date = date.toJavaLocalDate(),
    amount = amount,
    category = TransactionCategory.valueOf(category)
)
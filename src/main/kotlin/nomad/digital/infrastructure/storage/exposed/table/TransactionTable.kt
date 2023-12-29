package nomad.digital.infrastructure.storage.exposed.table

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.date

object TransactionTable : LongIdTable("TRANSACTION") {
    val name = varchar("name", 2048)
    val amount = decimal("amount", 10, 2)
    val category = varchar("category", 2048)
    val date = date("date")

    val account =
        reference(
            name = "account_id",
            foreign = AccountTable,
            onDelete = ReferenceOption.CASCADE,
            onUpdate = ReferenceOption.CASCADE,
        )
}

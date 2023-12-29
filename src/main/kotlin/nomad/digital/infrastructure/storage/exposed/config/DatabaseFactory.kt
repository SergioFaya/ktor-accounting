package nomad.digital.infrastructure.storage.exposed.config

import kotlinx.coroutines.Dispatchers
import nomad.digital.infrastructure.storage.exposed.table.AccountTable
import nomad.digital.infrastructure.storage.exposed.table.TransactionTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    lateinit var database: Database

    fun init() {
        val driverClassName = "org.h2.Driver"
        val jdbcURL = "jdbc:h2:file:./build/db"
        database = Database.connect(jdbcURL, driverClassName)

        transaction(database) {
            SchemaUtils.create(AccountTable)
            SchemaUtils.create(TransactionTable)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction(context = Dispatchers.IO, db = database) { block() }
}

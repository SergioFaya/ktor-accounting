package nomad.digital.infrastructure.storage.exposed.config

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import nomad.digital.domain.Account
import nomad.digital.domain.Transaction
import nomad.digital.domain.TransactionCategory
import nomad.digital.infrastructure.storage.exposed.addAccount
import nomad.digital.infrastructure.storage.exposed.table.AccountTable
import nomad.digital.infrastructure.storage.exposed.table.TransactionTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.LocalDate

object DatabaseFactory {

    lateinit var database: Database

    fun init() {
        val driverClassName = "org.h2.Driver"
        val jdbcURL = "jdbc:h2:file:./build/db"
        database = Database.connect(jdbcURL, driverClassName)

        val transactions = listOf(
            Transaction(
                id = 1,
                name = "gasto random",
                date = LocalDate.now(),
                amount = BigDecimal("1234.123"),
                category = TransactionCategory.PARTY
            ), Transaction(
                id = 2,
                name = "sueldo",
                date = LocalDate.now(),
                amount = BigDecimal("120000.99"),
                category = TransactionCategory.OTHER
            ), Transaction(
                id = 3,
                name = "restaurante de lujo",
                date = LocalDate.now(),
                amount = BigDecimal("1234.123"),
                category = TransactionCategory.FOOD
            )
        )
        val accountSabadell = Account(
            id = 1,
            title = "Sabadell",
            transactions = transactions
        )
        val accountIndexa = Account(
            id = 2,
            title = "Indexa"
        )

        transaction(database) {
            SchemaUtils.create(AccountTable)
            SchemaUtils.create(TransactionTable)
        }

        runBlocking {
            addAccount(accountIndexa)
            addAccount(accountSabadell)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(context = Dispatchers.IO, db = database) { block() }
}
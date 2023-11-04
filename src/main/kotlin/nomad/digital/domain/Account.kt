package nomad.digital.domain

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import nomad.digital.domain.TransactionCategory.OTHER
import java.math.BigDecimal
import java.time.LocalDate

@Serializable
data class Account(
    val id: Long,
    val title: String,
    val transactions: List<Transaction> = emptyList()
)

@Serializable
data class Transaction(
    val id: Long,
    val name: String,
    @Contextual
    val date: LocalDate,
    @Contextual
    val amount: BigDecimal,
    val category: TransactionCategory = OTHER
)

@Serializable
enum class TransactionCategory {
    SAVING, FOOD, PARTY, OTHER
}
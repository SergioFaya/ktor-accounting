package nomad.digital.domain

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class Account(
    val id: Long? = null,
    val accountName: String,
    val accountTransactions: List<AccountTransaction> = emptyList(),
)

@Serializable
data class AccountTransaction(
    val id: Long? = null,
    val concept: String,
    @Contextual
    val date: LocalDate,
    @Contextual
    val amount: BigDecimal,
    val category: TransactionCategory = TransactionCategory.UNSET,
)

@Serializable
enum class TransactionCategory {
    SAVING,
    FOOD,
    PARTY,
    OTHER,
    UNSET,
}

@Serializable
data class CustomCategory(
    val id: Long? = null,
    val name: String,
)

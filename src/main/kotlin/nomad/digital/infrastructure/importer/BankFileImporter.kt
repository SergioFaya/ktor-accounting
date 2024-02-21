package nomad.digital.infrastructure.importer

import nomad.digital.domain.TransactionDocumentType
import java.io.InputStream

sealed interface BankFileImporter {
    suspend fun InputStream.readBankTransactions(
        accountId: Long,
        type: TransactionDocumentType,
    )
}

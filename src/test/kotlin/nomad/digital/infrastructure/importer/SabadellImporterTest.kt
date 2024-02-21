package nomad.digital.infrastructure.importer

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.slot
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import nomad.digital.domain.AccountTransaction
import nomad.digital.domain.TransactionDocumentType
import nomad.digital.infrastructure.importer.SabadellImporter.readBankTransactions
import nomad.digital.infrastructure.storage.exposed.batchInsertAccountTransactions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SabadellImporterTest {

    companion object {
        private const val FILE_PATH = "/infrastructure/importer/sabadell/sabadell_valid_file.xlsx"
        private const val ACCOUNT_ID = 123L

        private val sabadellDocumentInputStream = this::class.java.getResourceAsStream(FILE_PATH)!!
    }

    @BeforeEach
    fun setUp() {
        mockkStatic(::batchInsertAccountTransactions)
        coEvery { batchInsertAccountTransactions(ACCOUNT_ID, any()) } just runs
    }

    @Test
    fun `should sabadell bank transactions`() =
        runTest {
            val slot = slot<List<AccountTransaction>>()
            sabadellDocumentInputStream.readBankTransactions(ACCOUNT_ID, TransactionDocumentType.EXCEL)

            coVerify { batchInsertAccountTransactions(ACCOUNT_ID, capture(slot)) }

            assertEquals(4, slot.captured.size)
        }
}

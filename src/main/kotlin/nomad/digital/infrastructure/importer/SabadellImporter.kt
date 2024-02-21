package nomad.digital.infrastructure.importer

import kotlinx.datetime.toKotlinLocalDate
import nomad.digital.domain.AccountTransaction
import nomad.digital.domain.TransactionCategory
import nomad.digital.domain.TransactionDocumentType
import nomad.digital.domain.TransactionDocumentType.EXCEL
import nomad.digital.infrastructure.storage.exposed.batchInsertAccountTransactions
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream
import java.math.BigDecimal

data object SabadellImporter : BankFileImporter {
    /**
     * TODO: add thread locking mechanism for several uploads at the time
     * consider deleting while uploading
     * F. Operativa,Concepto,F. Valor,Importe,Saldo,Referencia 1,Referencia 2
     * 30/10/2023,COMPRA TARJ LIDL MAD-MERCADO TETU\-MADRID,31/10/2023,"-7,36","4.715,40",,3333_3333
     */
    override suspend fun InputStream.readBankTransactions(
        accountId: Long,
        type: TransactionDocumentType,
    ) {
        when (type) {
            EXCEL -> readExcelTransactions(accountId)
            else -> UnsupportedOperationException("$type is not supported when loading bank transactions")
        }
    }

    private suspend fun InputStream.readExcelTransactions(accountId: Long) {
        val batchSize = 20
        val reader = this.bufferedReader()

        val workbook = WorkbookFactory.create(this)

        val worksheet = workbook.getSheetAt(0)

        // Iterate through each rows one by one
        val rowIterator: Iterator<Row> = worksheet.iterator()

        val transactions: MutableList<AccountTransaction> = mutableListOf()

        var empty = false
        while (rowIterator.hasNext() && !empty) {
            val row = rowIterator.next()
            if (row.getCell(0) == null || row.getCell(0).stringCellValue.isEmpty()) {
                empty = true
            } else {
                val dateUnparsed = row.getCell(0).stringCellValue
                val date = dateUnparsed.parseAsDate()

                val amount = BigDecimal(row.getCell(3).numericCellValue)

                transactions.add(
                    AccountTransaction(
                        concept = row.getCell(1).stringCellValue,
                        date = date.toKotlinLocalDate(),
                        amount = amount,
                        category = TransactionCategory.UNSET,
                    ),
                )
            }
        }
        batchInsertAccountTransactions(accountId, transactions)
    }
}

package nomad.digital.infrastructure

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.toKotlinLocalDate
import nomad.digital.domain.AccountTransaction
import nomad.digital.domain.TransactionCategory
import nomad.digital.infrastructure.storage.exposed.batchInsertAccountTransactions
import nomad.digital.infrastructure.storage.exposed.table.TransactionTable.amount
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class TransactionDocumentType {
    CSV,
    EXCEL,
}

private fun String.parseAsNumber(): BigDecimal {
    val symbols = DecimalFormatSymbols()
    symbols.groupingSeparator = '.'
    symbols.decimalSeparator = ','
    val pattern = "#,##0.0#"
    val decimalFormat = DecimalFormat(pattern, symbols)
    decimalFormat.isParseBigDecimal = true

    return decimalFormat.parse(this) as BigDecimal
}

/**
 * TODO: add thread locking mechanism for several uploads at the time
 * consider deleting while uploading
 * F. Operativa,Concepto,F. Valor,Importe,Saldo,Referencia 1,Referencia 2
 * 30/10/2023,COMPRA TARJ LIDL MAD-MERCADO TETU\-MADRID,31/10/2023,"-7,36","4.715,40",,3333_3333
 */
suspend fun InputStream.readBankTransactions(
    accountId: Long,
    type: TransactionDocumentType = TransactionDocumentType.EXCEL,
) {
    when (type) {
        TransactionDocumentType.CSV -> readCsvTransactions(accountId)
        TransactionDocumentType.EXCEL -> readExcelTransactions(accountId)
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
        if (row.getCell(0) == null || row.getCell(0).getStringCellValue().isEmpty()) {
            empty = true
        } else {
            val dateUnparsed = row.getCell(0).getStringCellValue()
            val date = LocalDate.parse(dateUnparsed, DateTimeFormatter.ofPattern("dd/MM/yyyy"))

            val amount = BigDecimal(row.getCell(3).getNumericCellValue())

            transactions.add(
                AccountTransaction(
                    concept = row.getCell(1).getStringCellValue(),
                    date = date.toKotlinLocalDate(),
                    amount = amount,
                    category = TransactionCategory.UNSET,
                ),
            )
        }
    }
    batchInsertAccountTransactions(accountId, transactions)
}

private suspend fun InputStream.readCsvTransactions(accountId: Long) {
    val batchSize = 20
    val reader = this.bufferedReader()

    withContext(Dispatchers.IO) {
        val transactions: List<AccountTransaction> =
            reader.readLines().map {
                val line = it
                with(line.split('\t')) {
                    val date = LocalDate.parse(this[0], DateTimeFormatter.ofPattern("dd/MM/yyyy"))

                    val amount = this[3].parseAsNumber()

                    AccountTransaction(
                        concept = this[1],
                        date = date.toKotlinLocalDate(),
                        amount = amount,
                        category = TransactionCategory.UNSET,
                    )
                }
            }

        batchInsertAccountTransactions(accountId, transactions)
    }
}

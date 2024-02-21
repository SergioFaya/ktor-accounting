package nomad.digital.infrastructure.importer

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Parses a pattern of a number from a excel file format.
fun String.parseAsNumber(): BigDecimal {
    val symbols = DecimalFormatSymbols()
    symbols.groupingSeparator = '.'
    symbols.decimalSeparator = ','
    val pattern = "#,##0.0#"
    val decimalFormat = DecimalFormat(pattern, symbols)
    decimalFormat.isParseBigDecimal = true

    return decimalFormat.parse(this) as BigDecimal
}

// Formats date with format dd/MM/yyyy.
fun String.parseAsDate(): LocalDate = LocalDate.parse(this, DateTimeFormatter.ofPattern("dd/MM/yyyy"))

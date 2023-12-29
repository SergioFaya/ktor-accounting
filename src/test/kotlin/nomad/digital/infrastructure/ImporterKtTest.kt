package nomad.digital.infrastructure

import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class ImporterKtTest {
    @TestFactory
    fun numberFormattingTest() =

        listOf(
            "12,00",
            "12,12",
            "3.016,16",
            "-10,37",
            "-0,10",
        ).map {
            dynamicTest("Test number formatting for value $it") {
                val symbols = DecimalFormatSymbols()
                symbols.groupingSeparator = '.'
                symbols.decimalSeparator = ','
                val pattern = "#,##0.0#"
                val decimalFormat = DecimalFormat(pattern, symbols)
                decimalFormat.isParseBigDecimal = true

                decimalFormat.parse(it) as BigDecimal
            }
        }
}

package nomad.digital.infrastructure

import nomad.digital.infrastructure.importer.parseAsNumber
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory

internal class UtilsKtTest {
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
                it.parseAsNumber()
            }
        }
}

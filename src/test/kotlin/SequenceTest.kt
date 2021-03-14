import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SequenceTest {

    /*
    Sequences do not creates temporary collections. It may be more efficient when processing large collections
    */
    @Test
    fun `Elements should be processed sequentially`(){

        val people = listOf(
                Person(name = "John", age = 15),
                Person(name = "Mike", age = 43),
                Person(name = "Brian", age = 84),
                Person(name = "Alice", age = 68),
                Person(name = "Bob", age = 53) )

        val trace = StringBuilder()
        val result = people.asSequence()
                .map { trace.append("${it.name} -> map\n"); it.name }
                .filter { trace.append("$it -> filter\n"); it.startsWith("B") }
                .toList()

        println(result)

        val expectedResult = """
        |John -> map
        |John -> filter
        |Mike -> map
        |Mike -> filter
        |Brian -> map
        |Brian -> filter
        |Alice -> map
        |Alice -> filter
        |Bob -> map
        |Bob -> filter
        """.trimMargin()+"\n"

        val actualResult = trace.toString()
        assertEquals(expectedResult, actualResult)
    }
}
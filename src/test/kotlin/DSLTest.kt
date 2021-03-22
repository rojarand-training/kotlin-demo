import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.lang.StringBuilder

class DSLTest {

    @Test
    fun `should create html table string`(){
        val htmlString = html {
            table {
                tr {
                }
                tr {
                    td {
                    }
                }
            }
        }.toHtml()

        assertEquals("<html><table><tr></tr><tr><td></td></tr></table></html>", htmlString)
        println(htmlString)
    }
}

fun html(htmlAction: Html.()->Unit) = Html(htmlAction)

open class HtmlTag{
    protected val innerTags = mutableListOf<HtmlTag>()
    fun toHtml(): String{
        val tagName = javaClass.name.toLowerCase()
        val innerHtml = StringBuilder()
        innerTags.forEach { innerHtml.append(it.toHtml()) }
        return "<$tagName>$innerHtml</$tagName>"
    }
}

class Html(htmlAction: Html.()->Unit): HtmlTag(){
    init { htmlAction() }
    fun table(tableAction: Table.() -> Unit) = innerTags.add(Table(tableAction))
}

class Table(action: Table.()->Unit):HtmlTag() {
    init { action() }
    fun tr(trAction: Tr.()->Unit) = innerTags.add(Tr(trAction))
}

class Tr(trAction: Tr.()->Unit):HtmlTag(){
    init { trAction() }
    fun td(tdAction: Td.()->Unit) = innerTags.add(Td(tdAction))
}

class Td(tdAction: Td.()->Unit): HtmlTag(){}

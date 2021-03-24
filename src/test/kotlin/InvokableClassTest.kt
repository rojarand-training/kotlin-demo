import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

enum class Platform{
    Android,
    iOS
}

data class Issue(val platform: Platform,
                 val type: String,
                 val description: String)

class ImportantIssuePredicate(private val platform: Platform)
    : (Issue)->Boolean{

    override fun invoke(issue: Issue): Boolean {
        return issue.isImportant() && issue.platform == platform
    }

    fun Issue.isImportant():Boolean{
        return type.toLowerCase() == "bug" ||
                type.toLowerCase() == "error" ||
                type.toLowerCase() == "critical"
    }
}
class InvokableClassTest {

    @Test
    fun `should find expected important issues for Android`() {

        val issue1 = Issue(Platform.Android,"BUG",  "Push notification not working")
        val issue2 = Issue(Platform.iOS,"Change request",  "Add 'skip' button")
        val issue3 = Issue(Platform.Android,"User story",  "")
        val issue4 = Issue(Platform.iOS,"Critical",  "Exception during processing")
        val issues = listOf(issue1, issue2, issue3, issue4)

        val issuePredicate = ImportantIssuePredicate(Platform.Android)
        val androidIssues = issues.filter(issuePredicate)

        assertEquals(1, androidIssues.size )
        assertEquals("BUG", androidIssues[0].type)
    }
}
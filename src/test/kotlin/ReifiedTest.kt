import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReifiedTest {

    @Nested
    @DisplayName("When function is 'inline' and 'reified'")
    class InlineAndReifiedGenericFunctionTest{

        private inline fun <reified T>isArgumentTypeOf(argument: Any):Boolean = argument is T

        @Test
        fun `type should be known in runtime`(){
            assertFalse(isArgumentTypeOf<String>(123))
            assertTrue(isArgumentTypeOf<String>("123"))
        }
    }

    @Nested
    @DisplayName("When function is not 'inline' and 'reified'")
    class NotInlineAndReifiedGenericFunctionTest{

        //Buggy implementation
        private fun <T>isArgumentTypeOf(argument: Any):Boolean {
            return try {
                argument as T
                true
            }
            catch (exc: ClassCastException){
               false
            }
        }

        @Test
        fun `type should not be known in runtime`(){
            //NOTE!!! Function testing whether type is String with argument int returns true
            assertTrue(isArgumentTypeOf<String>(123))

            assertTrue(isArgumentTypeOf<String>("123"))
        }
    }
}
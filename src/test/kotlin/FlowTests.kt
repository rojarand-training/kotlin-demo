import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

//Base on https://kotlinlang.org/docs/flow.html
class FlowTests {

    private fun log(msg: String) = println("[${Thread.currentThread().name}] $msg")

    /**
     * In coroutines, a flow is a type that can emit multiple values sequentially,
     * as opposed to suspend functions that return only a single value.
     * Flows are cold streams similar to sequences — the code inside a flow builder does not run until the flow is collected.
     * */

    private val emitRange = 1..3
    private val emitDelay = 10L

    private fun simple(): Flow<Int> = flow { // flow builder
        for (i in emitRange) {
            delay(emitDelay) // pretend we are doing something useful here
            emit(i) // emit next value
        }
    }

    @Test
    fun `Collects only a few items`(): Unit = runBlocking {
        var collected = 0
        withTimeoutOrNull((emitDelay*(emitRange.count()-1))) { // Timeout after 25ms
            simple().collect { value ->
                collected += 1
                println(value)
            }
        }
        assertTrue(collected < emitRange.count())
    }

    @Test
    fun `Show simple flow in action`() = runBlocking {

        launch {
            for (i in emitRange) {
                delay(21)
                println("Hello from launch: $i")
            }
        }

        simple().collect {
            println("Element received: $it")
        }

        /**
        Element received: 1
        Hello from launch: 1
        Element received: 2
        Element received: 3
        Hello from launch: 2
        Hello from launch: 3
         **/
    }

    @Test
    fun `Takes only 2 elements`() = runBlocking {
        var counter = 0
        simple().take(2).collect {
            counter += 1
        }
        assertEquals(2, counter)
    }

    @Test
    fun `Fold, reduce, first or toList are 'terminal flow operators' that start the collection`() = runBlocking {
        val foldRes = simple().fold(0) { acc, _ -> acc+1}
        assertEquals(emitRange.count(), foldRes)

        val first = simple().first()
        assertEquals(emitRange.first, first)
    }

    @Test
    fun `Each individual collection of a flow is performed sequentially`() = runBlocking {
        (1..5).asFlow()
                .filter {
                    println("Filter $it")
                    it % 2 == 0
                }
                .map {
                    println("Map $it")
                    "string $it"
                }.collect {
                    println("Collect $it")
                }

        /**
        Filter 1
        Filter 2
        Map 2
        Collect string 2
        Filter 3
        Filter 4
        Map 4
        Collect string 4
        Filter 5
         * */
    }

    @Test
    fun `Runs a flow in different coroutine context that it is collected`() = runBlocking {
        fun localSimple(): Flow<Int> = flow {
            for (i in 1..3) {
                Thread.sleep(100) // pretend we are computing it in CPU-consuming way
                log("Emitting $i")
                emit(i) // emit next value
            }
        }.flowOn(Dispatchers.Default)

        localSimple().collect { value ->
            log("Collected $value")
        }

        /**
        [DefaultDispatcher-worker-1 @coroutine#2] Emitting 1
        [Test worker @coroutine#1] Collected 1
        [DefaultDispatcher-worker-1 @coroutine#2] Emitting 2
        [Test worker @coroutine#1] Collected 2
        [DefaultDispatcher-worker-1 @coroutine#2] Emitting 3
        [Test worker @coroutine#1] Collected 3
         * */
    }

    @Test
    fun `Conflation skips intermediate values`() = runBlocking {
        var counter = 0
        simple().conflate()
                .collect {
            println("Collected: $it")
            delay((emitDelay*2.5).toLong())
            counter += 1
        }
        assertTrue(counter < emitRange.count())
    }

    @Test
    fun `Zips two streams`() = runBlocking {
        fun delayedIntegers():Flow<Int> = flow {
            emit(1)
            delay(10)
            emit(2)
            delay(10)
            emit(3)
        }
        fun strings(): Flow<String> = flow {
            emit("a")
            emit("b")
            emit("c")
        }
        val res = delayedIntegers().zip(strings()) {integer, string -> "$integer:$string"}
                .fold("") {acc, value -> "$acc$value," }
        assertEquals("1:a,2:b,3:c,", res)
    }

    @Test
    fun `Catches exception raised in terminal operator`() = runBlocking {

        fun localSimple(): Flow<Int> =
            flow {
                for (i in 1..3) {
                    println("Emitting $i")
                    emit(i) // emit next value
                }
            }

        try {
            localSimple().collect { value ->
                println(value)
                check(value <= 1) { "Collected $value" }
            }
        } catch (e: Throwable) {
            println("Caught $e")
        }
    }

    @Test
    fun `Catches exception raised in flow`() = runBlocking {

        fun localSimple(): Flow<String> =
                flow {
                    for (i in 1..3) {
                        println("Emitting $i")
                        emit(i) // emit next value
                    }
                }
                .map { value ->
                    check(value <= 1) { "Crashed on $value" }
                    "string $value"
                }

        try {
            localSimple().collect { value ->
                println(value)
            }
        } catch (e: Throwable) {
            println("Caught $e")
        }
    }

    @Test
    fun `An upstream exception is caught by the 'catch' operator`() = runBlocking {
        fun localSimple(): Flow<String> =
            flow {
                for (i in 1..3) {
                    println("Emitting $i")
                    emit(i) // emit next value
                }
            }
            .map { value ->
                check(value <= 1) { "Crashed on $value" }
                "string $value"
            }

        localSimple()
        .catch { e -> emit("Caught $e") } // emit on exception
        .collect { value -> println(value) }
        /**
        Emitting 1
        string 1
        Emitting 2
        Caught java.lang.IllegalStateException: Crashed on 2
         * */
    }

    @Test
    fun `An downstream exception is not caught by the 'catch' operator`() = runBlocking {
        try {
            fun localSimple(): Flow<Int> =
                    flow {
                        for (i in 1..3) {
                            println("Emitting $i")
                            emit(i) // emit next value
                        }
                    }

            localSimple()
                    .catch { e -> println("Caught by catch operator $e") } // emit on exception
                    .collect { value ->
                        check(value <= 1) { "Collected $value" }
                        println(value)
                    }
        } catch (e: Throwable) {
            println("Caught by catch statement $e")
        }
        //Caught by catch statement java.lang.IllegalStateException: Collected 2
    }

    @Test
    fun `The completion operator is called when flow ended`() = runBlocking {
        fun localSimple(): Flow<Int> = flow {
            emit(1)
            throw RuntimeException()
        }
        localSimple()
            .onCompletion { cause -> if (cause != null) println("Flow completed exceptionally") }
            .catch { cause -> println("Caught exception") }
            .collect { value -> println(value) }
        //Flow completed exceptionally
    }

    @Test
    fun `Non cancellable flow does not stop executing`() {
        var last = 0
        try {
            runBlocking {
                (1..5).asFlow().collect { value ->
                    println(value)
                    last = value
                    if (value > 3) cancel()
                }
            }
        } catch (e: Throwable) {
        }
        assertEquals(5, last)
    }

    @Test
    fun `Cancellable flow does stop executing`()  {
        var last = 0
        try {
            runBlocking {
                (1..5).asFlow().cancellable().collect { value ->
                    println(value)
                    last = value
                    if (value >= 3) cancel()
                }
            }
        } catch (e: Throwable) {
        }
        assertEquals(3, last)
    }
}
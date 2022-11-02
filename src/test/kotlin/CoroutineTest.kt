import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

/**
Coroutines scope
Defines a scope for new coroutines. Every coroutine builder (like launch, async, etc.) is an extension on CoroutineScope
and inherits its coroutineContext to automatically propagate all its elements and cancellation.

class MyUIClass {
    val scope = MainScope() // the scope of MyUIClass, uses Dispatchers.Main

    fun destroy() { // destroys an instance of MyUIClass
        scope.cancel() // cancels all coroutines launched in this scope
    }
    fun showSomeData() = scope.launch { // launched in the main thread
           // ... here we can use suspending functions or coroutine builders with other dispatchers
           draw(data) // draw in the main thread
        }
    }
}

- Coroutine scope and does not complete until all launched children complete.

Builders: runBlocking, coroutineScope

runBlocking and coroutineScope builders may look similar because they both wait for their body and all its children to complete.
The main difference is that the runBlocking method blocks the current thread for waiting, while coroutineScope just suspends,
releasing the underlying thread for other usages.

*/

/**
A global CoroutineScope not bound to any job.
Global scope is used to launch top-level coroutines which are operating on the whole application lifetime and are not cancelled prematurely.
 *
 * */

/**
Dispatchers:
- Default dispatcher is used when no other dispatcher is explicitly specified in the scope. Uses a shared background pool of threads.

- Main dispatcher is not confined to any specific thread. It executes initial continuation of the coroutine in the current call-frame and lets the coroutine resume in whatever thread that is used by the corresponding suspending function, without mandating any specific threading policy.
Depending on platform and classpath, it can be mapped to different dispatchers:
On JS and Native it is equivalent to the Default dispatcher.
On JVM it is either the Android main thread dispatcher, JavaFx or Swing EDT dispatcher. It is chosen by the ServiceLoader.

- IO dispatcher is designed for offloading blocking IO tasks to a shared pool of threads.

- Unconfined dispatcher starts a coroutine in the caller thread, but only until the first suspension point.
After suspension it resumes the coroutine in the thread that is fully determined by the suspending function that was invoked.

launch(Dispatchers.Unconfined) { // not confined -- will work with main thread
    println("Unconfined      : I'm working in thread ${Thread.currentThread().name}")
    delay(500)
    println("Unconfined      : After delay in thread ${Thread.currentThread().name}")
}

Unconfined      : I'm working in thread main
Unconfined      : After delay in thread kotlinx.coroutines.DefaultExecutor
*/

class CoroutineTest {


    @DisplayName("When running coroutines with async")
    class AsyncTest {

        private val defaultDurationOfOneComputation = 10L

        @Test
        fun `Should compute expected result parallelly`() = runBlocking {
            //given
            val numberOfComputations = 1000
            val expectedResultOfComputation = numberOfComputations * 1
            //when
            val computations = (1..numberOfComputations).map {
                async() { pretendComputationWithDuration(defaultDurationOfOneComputation) }
            }

            val actualResultOfComputation = computations.awaitAll().sum()
            //then
            assertEquals(expectedResultOfComputation, actualResultOfComputation)
        }

        @Test
        fun `Parallel computation should be quicker than serial`() = runBlocking {
            //given
            val numberOfComputations = 1000
            val durationOfParallelComputation = measureTimeMillis {
                //when
                val computations = (1..numberOfComputations).map {
                    async() { pretendComputationWithDuration(defaultDurationOfOneComputation) }
                }
                val resultOfComputation = computations.awaitAll().sum()
                assertEquals(numberOfComputations, resultOfComputation)
            }
            //then
            val durationOfSerialComputation = defaultDurationOfOneComputation * numberOfComputations
            assertTrue(durationOfParallelComputation < durationOfSerialComputation)
        }

        @Test
        fun `All computations should be cancellable at once`() = runBlocking {
            //given
            val numberOfComputations = 1000_000
            var computations: List<Deferred<Int>>? = null
            val job = launch {
                computations = (1..numberOfComputations).map {
                    async() { pretendComputationWithDuration(0) }
                }
            }
            //when
            job.cancel()
            //then
            val resultOfComputation = computations?.awaitAll()?.sum() ?: 0
            assertTrue(resultOfComputation < numberOfComputations)
        }

        private suspend fun pretendComputationWithDuration(durationOfOneComputation: Long): Int {
            delay(durationOfOneComputation)
            return 1
        }
    }

    @Test
    fun `should cancel all nested coroutines`() = runBlocking {
        val counter = AtomicInteger()
        val numberOfWorkingCoroutines = 2
        val countDownLatch = CountDownLatch(numberOfWorkingCoroutines)
        val job = launch(Dispatchers.IO) {
            launch {
                while (isActive) {
                    counter.incrementAndGet()
                    countDownLatch.countDown()
                    delay(Long.MAX_VALUE)
                }
            }
            launch {
                while (isActive) {
                    counter.incrementAndGet()
                    countDownLatch.countDown()
                    delay(Long.MAX_VALUE)
                }
            }
        }
        countDownLatch.await()
        job.cancel()
        assertEquals(numberOfWorkingCoroutines, counter.get())
    }

    @Test
    fun `should receive all sent numbers`() = runBlocking {
        val channel = Channel<Int>()
        val jobs = mutableListOf<Job>()
        val numbersToSend = listOf(1, 2, 3, 4, 5)
        jobs += launch {
            numbersToSend.forEach {
                channel.send(it)
                delay(10)
            }
            channel.close()
        }

        var actualNumberOfReceivedNumbers = 0
        jobs += launch {
            for (i in channel) {
                actualNumberOfReceivedNumbers += 1
            }
        }
        jobs.joinAll()
        assertEquals(numbersToSend.count(), actualNumberOfReceivedNumbers)
    }

}

/*
internal class SequentialJobQueue(private val lifecycle: Lifecycle) {

    private val channel = Channel<Job>(capacity = Channel.UNLIMITED).apply {
        lifecycle.coroutineScope.launch(Dispatchers.IO) {
            consumeEach { it.join() }
        }
    }

    fun enqueueJob(jobLogic: suspend (CoroutineScope) -> Unit) = channel.trySend(job(jobLogic))

    fun scheduleJob(delay: Duration, jobLogic: suspend (CoroutineScope) -> Unit) {
        lifecycle.coroutineScope.launch {
            delay(delay.ms)
            enqueueJob(jobLogic)
        }
    }

    private fun job(block: suspend (CoroutineScope) -> Unit): Job {
        /*
        Lazy way of executing jobs allows to execute jobs sequentially. We want to modify temperatures
        without concurrent access (sequentially) to avoid synchronization issues.
        //https://stackoverflow.com/questions/56480520/kotlin-coroutines-sequential-execution
        */
        return lifecycle.coroutineScope.launch(
            start = CoroutineStart.LAZY,
            context = Dispatchers.IO
        ) {
            block(this)
        }
    }
}
*/
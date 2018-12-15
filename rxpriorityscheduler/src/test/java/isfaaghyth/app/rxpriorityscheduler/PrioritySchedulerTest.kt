package isfaaghyth.app.rxpriorityscheduler

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import java.util.ArrayList
import java.util.Collections
import java.util.HashSet
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

import io.reactivex.Observable
import io.reactivex.functions.Consumer
import io.reactivex.Scheduler

import org.junit.Assert.fail
import org.junit.Assert.assertEquals

/**
 * Created by isfaaghyth on 15/12/18.
 * github: @isfaaghyth
 */
@RunWith(JUnit4::class)
class PrioritySchedulerTest {

    @Test
    fun schedulesInOrderOfPriorityWhenSingleThreaded() {
        val parallelism = 1
        val scheduler = PriorityScheduler.withConcurrency(parallelism)

        val count = 1000
        val finishLatch = CountDownLatch(count)
        val loopLatch = CountDownLatch(1)
        val actual = Collections.synchronizedList(ArrayList<Int>())
        val onNextLock = Any()

        for (i in 0..count) {
            Observable.just(i)
                .subscribeOn(scheduler.priority(i))
                .subscribe { res ->
                    run {
                        synchronized(onNextLock) {
                            await(loopLatch)
                            actual.add(res)
                            finishLatch.countDown()
                        }
                    }
                }
        }
        loopLatch.countDown()
        finishLatch.await()

        val subList = actual.subList(parallelism, actual.size)
        var last = Int.MAX_VALUE

        for (i in subList) {
            if (last < i) {
                fail("actual was not monotonically decreasing after the first N items, where N " +
                        "is the scheduler's parallelism. failed at index ${actual.indexOf(i)}" +
                        " (value = $i). Full List: $actual")
            }
            last = i
        }
    }

    fun await(latch: CountDownLatch) {
        try {
            latch.await()
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

}
package isfaaghyth.app.rxpriorityscheduler

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.internal.schedulers.ScheduledRunnable

import java.util.concurrent.TimeUnit.MILLISECONDS

/**
 * A class to be used with RxJava's {@link Scheduler} interface. Though this class is not a {@link
 * Scheduler} itself, calling {@link #priority(int)} will return one. E.x.: {@code PriorityScheduler
 * scheduler = new PriorityScheduler(); Observable.just(1, 2, 3) .subscribeOn(scheduler.priority(10))
 * .subscribe(); }
 * Created by isfaaghyth on 15/12/18.
 * github: @isfaaghyth
 * ported: ronshapiro/rxjava-priority-scheduler (RxJava v.1)
 */
class PriorityScheduler(val concurrency: Int) {

    private val queue = PriorityBlockingQueue<ComparableRunnable>()

    /**
     * Creates a {@link PriorityScheduler} with as many threads as the machine's available
     * processors.
     * <p>
     * <b>Note:</b> this does not ensure that the priorities will be adheared to exactly, as the
     * JVM's threading policy might allow one thread to dequeue an action, then let a second thread
     * dequeue the next action, run it, dequeue another, run it, etc. before the first thread runs
     * its action. It does however ensure that at the dequeue step, the thread will receive the
     * highest priority action available.
     */
    fun create(): PriorityScheduler = PriorityScheduler(Runtime.getRuntime().availableProcessors())

    /**
     * Creates a {@link PriorityScheduler} using at most {@code concurrency} concurrent actions.
     * <p>
     * <b>Note:</b> this does not ensure that the priorities will be adheared to exactly, as the
     * JVM's threading policy might allow one thread to dequeue an action, then let a second thread
     * dequeue the next action, run it, dequeue another, run it, etc. before the first thread runs
     * its action. It does however ensure that at the dequeue step, the thread will receive the
     * highest priority action available.
     */
    fun withConcurrency(concurrency: Int): PriorityScheduler = PriorityScheduler(concurrency)

    /**
     * Getter
     */
    fun get(): PriorityScheduler = create()


    class ComparableRunnable(private val runnable: Runnable, private val priority: Int): Runnable, Comparable<ComparableRunnable> {
        override fun run() = runnable.run()
        override fun compareTo(other: ComparableRunnable): Int = other.priority - priority
    }

}
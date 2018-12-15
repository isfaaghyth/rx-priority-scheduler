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
class PriorityScheduler(private var concurrency: Int = 0) {

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
    companion object {
        fun withConcurrency(concurrency: Int): PriorityScheduler = PriorityScheduler(concurrency)
    }

    /**
     * Getter
     */
    fun get(): PriorityScheduler = create()

    /**
     * Prioritize {@link io.reactivex.functions.Action  action}s with a numerical priority
     * value. The higher the priority, the sooner it will run.
     */
    fun priority(priority: Int): Scheduler = InnerPriorityScheduler(priority, concurrency, queue)

    class InnerPriorityScheduler(
        private val priority: Int,
        private val concurrency: Int,
        private val queue: PriorityBlockingQueue<ComparableRunnable>): Scheduler() {

        private val workerCount = AtomicInteger()
        private var executorService: ExecutorService = Executors.newFixedThreadPool(concurrency)

        override fun createWorker(): Worker {
            synchronized(workerCount) {
                if (workerCount.get() < concurrency) {
                    workerCount.incrementAndGet()
                    executorService.submit {
                        while (true) {
                            try {
                                val runnable = queue.take()
                                runnable.run()
                            } catch (e: InterruptedException) {
                                Thread.currentThread().interrupt()
                                break
                            }
                        }
                    }
                }
            }
            return PriorityWorker(queue, priority)
        }

    }

    class PriorityWorker(
        val queue: PriorityBlockingQueue<ComparableRunnable>,
        private val priority: Int
    ): Scheduler.Worker() {

        private val compositeDisposable = CompositeDisposable()

        override fun isDisposed(): Boolean = compositeDisposable.isDisposed

        override fun schedule(run: Runnable): Disposable {
            return schedule(run, 0, MILLISECONDS)
        }

        /**
         * inspired by HandlerThreadScheduler.InnerHandlerThreadScheduler#schedule.
         * @see <a href="https://github.com/ReactiveX/RxAndroid/blob/53bc70785b1c8f150c2be871a5b85979ad8b233a/src/main/java/rx/android/schedulers/HandlerThreadScheduler.java">InnerHandlerThreadScheduler</a>
         */
        override fun schedule(run: Runnable, delay: Long, unit: TimeUnit): Disposable {
            val runnable = ComparableRunnable(run, priority)
            val scheduledRunnable = ScheduledRunnable(runnable, compositeDisposable)
            scheduledRunnable.setFuture(object : Future<Any> {
                override fun cancel(mayInterruptIfRunning: Boolean): Boolean = queue.remove(runnable)
                override fun isCancelled(): Boolean = false
                override fun isDone(): Boolean = false
                override fun get(timeout: Long, unit: TimeUnit?): Any = Unit
                override fun get(): Any = Unit

            })
            compositeDisposable.add(scheduledRunnable)
            queue.offer(runnable, delay, unit)
            return scheduledRunnable
        }

        override fun dispose() = compositeDisposable.dispose()

    }

    class ComparableRunnable(private val runnable: Runnable, private val priority: Int): Runnable, Comparable<ComparableRunnable> {
        override fun run() = runnable.run()
        override fun compareTo(other: ComparableRunnable): Int = other.priority - priority
    }

}
package org.variantsync.diffdetective.parallel;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.*;

/**
 * An iterator over the results of computations run in other threads.
 * The results of the given tasks can be received in the correct order using {@link next}. That
 * means the {@link next} method is deterministic if all tasks are deterministic.
 *
 * <p>Upon creation of a ScheduledTasksIterator, all given tasks are scheduled for execution. However, the results of these
 * tasks become available in the same order as the tasks have been provided. This means, that the iterator will block
 * until the result of the next task is available, even if all other remaining tasks are complete.
 */
public class ScheduledTasksIterator<T> implements Iterator<T>, AutoCloseable {
    private final Iterator<? extends Callable<T>> remainingTasks;
    private final LinkedList<Future<T>> futures;
    private final ExecutorService threadPool;

    /**
     * Starts scheduling {@code tasks} in {@code nThreads} other threads.
     * The results of these tasks can be retrieved by calling {@link next}. The order of these
     * results is deterministic and is the same as the order of the provided {@code tasks}.
     *
     * @param tasks the tasks which will be executed in other threads
     * @param nThreads the number of threads which work on {@code tasks} in parallel
     */
    public ScheduledTasksIterator(final Iterator<? extends Callable<T>> tasks, final int nThreads) {
        this.remainingTasks = tasks;
        this.futures = new LinkedList<>();
        this.threadPool = Executors.newFixedThreadPool(nThreads);
        while (this.remainingTasks.hasNext()) {
            futures.add(threadPool.submit(remainingTasks.next()));
        }
    }

    /**
     * Starts scheduling {@code tasks} in {@code nThreads} other threads.
     *
     * @param tasks the tasks which will be executed in other threads
     * @param nThreads the number of threads which work on {@code tasks} in parallel
     */
    public ScheduledTasksIterator(final Iterable<? extends Callable<T>> tasks, final int nThreads) {
        this(tasks.iterator(), nThreads);
    }

    @Override
    public boolean hasNext() {
        return !futures.isEmpty();
    }

    /**
     * Waits for the next task and retrieves its result.
     * The order of the results corresponds to the order of tasks given in the
     * {@link ScheduledTasksIterator constructor}. Each call to {@code next} schedules a new task
     * if any task is remaining.
     *
     * @return one result of a tasks given in {@link ScheduledTasksIterator}
     * @throws RuntimeException if a thread is interrupted or a task couldn't be executed
     */
    @Override
    public T next() {
        try {
            return futures.removeFirst().get();
        } catch (final InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /** Stops all scheduled tasks and releases the used thread resources. */
    @Override
    public void close() throws Exception {
        threadPool.shutdown();
    }
}

package parallel;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.*;

public class ScheduledTasksIterator<T> implements Iterator<T>, AutoCloseable {
    private final Iterator<? extends Callable<T>> remainingTasks;
    private final LinkedList<Future<T>> futures;
    private final ExecutorService threadPool;

    public ScheduledTasksIterator(final Iterator<? extends Callable<T>> tasks, final int nThreads) {
        this.remainingTasks = tasks;
        this.futures = new LinkedList<>();
        this.threadPool = Executors.newFixedThreadPool(nThreads);
        for (int i = 0; i < nThreads; i++) {
            scheduleNext();
        }
    }

    public ScheduledTasksIterator(final Iterable<? extends Callable<T>> tasks, final int nThreads) {
        this(tasks.iterator(), nThreads);
    }

    private synchronized void scheduleNext() {
        if (this.remainingTasks.hasNext()) {
            futures.add(threadPool.submit(remainingTasks.next()));
        }
    }

    @Override
    public boolean hasNext() {
        return !futures.isEmpty();
    }

    @Override
    public T next() {
        scheduleNext();
        try {
            return futures.removeFirst().get();
        } catch (final InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        threadPool.shutdown();
    }
}

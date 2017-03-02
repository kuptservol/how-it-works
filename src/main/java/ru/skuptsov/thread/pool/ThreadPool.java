package ru.skuptsov.thread.pool;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

/**
 * @author Sergey Kuptsov
 * @since 02/03/2017
 */
public class ThreadPool implements Executor {
    private final static Queue<Runnable> workQueue = new ConcurrentLinkedQueue<>();
    private static volatile boolean isStopped = false;

    public ThreadPool(int nThreads) {
        for (int i = 0; i < nThreads; i++) {
            new Thread(new TaskWorker()).start();
        }
    }

    @Override
    public void execute(Runnable command) {
        workQueue.offer(command);
    }

    public void shutdown() {
        isStopped = true;
    }

    private final class TaskWorker implements Runnable {

        @Override
        public void run() {
            while (!isStopped) {
                Runnable nextTask = workQueue.poll();
                if (nextTask != null) {
                    nextTask.run();
                }
            }
        }
    }
}

package com.ck.dev.punjabify.threads;

import android.os.Process;

import androidx.annotation.NonNull;

import com.ck.dev.punjabify.utils.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolManager {

    private static final ThreadPoolManager instance;

    private static final int KEEP_ALIVE_TIME = 10;

    private static final TimeUnit KEEP_ALIVE_TIME_UNIT;

    private final ExecutorService executorService;
    private final BlockingQueue<Runnable> taskQueue;
    private final List<Future> runningTaskList;

    static {
        KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
        instance = new ThreadPoolManager();
    }

    private ThreadPoolManager() {
        taskQueue = new LinkedBlockingQueue<>();
        runningTaskList = new ArrayList<>();
        int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
        executorService = new ThreadPoolExecutor(
                NUMBER_OF_CORES,
                NUMBER_OF_CORES * 2,
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                taskQueue,
                new BackgroundThreadFactory());
    }

    public static ThreadPoolManager getInstance() {
        return instance;
    }

    public void addCallable(Runnable runnable, String tag) {
        Future future = executorService.submit(runnable);
        runningTaskList.add(future);
    }

    public void cancelAllTasks() {
        synchronized (this) {
            taskQueue.clear();
            Config.LOG(Config.TAG_THREAD, "Cancel Threads ", true);
            for (Future task : runningTaskList) {
                if (!task.isDone()) {
                    task.cancel(true);
                }
            }
            runningTaskList.clear();
        }
    }

    private static class BackgroundThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(@NonNull Runnable r) {
            Thread thread = new Thread(r);
            int tag = 1;
            thread.setName("Punjabify_Thread" + tag);
            thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
            thread.setUncaughtExceptionHandler((t, e) -> Config.LOG(Config.TAG_THREAD, "Thread Error TAG : " + t.getName() + " -> " + e.getMessage(), true));
            return thread;
        }
    }
}

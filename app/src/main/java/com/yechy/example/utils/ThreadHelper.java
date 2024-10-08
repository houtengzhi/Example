package com.yechy.example.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.CallSuper;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by cloud on 2019-10-12.
 */
public class ThreadHelper {

    private static final Handler HANDLER = new Handler(Looper.getMainLooper());

    private static final Map<Integer, Map<Integer, ExecutorService>> TYPE_PRIORITY_POOLS = new HashMap<>();

    private static final Map<Integer, ScheduledExecutorService> SCHEDULED_POOLS = new HashMap<>();

    private static final Map<Task, ExecutorService> TASK_POOL_MAP = new ConcurrentHashMap<>();

    private static final Map<Task<?>, Future<?>> TASK_FUTURE_MAP = new ConcurrentHashMap<>();

    private static final int   CPU_COUNT = Runtime.getRuntime().availableProcessors();

    private static final byte TYPE_SINGLE = -1;
    private static final byte TYPE_CACHED = -2;
    private static final byte TYPE_IO     = -4;
    private static final byte TYPE_CPU    = -8;

    private static Executor sDeliver;

    private static ExecutorService sDelegate;

    /**
     * Return whether the thread is the main thread.
     *
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public static Handler getMainHandler() {
        return HANDLER;
    }

    public static void runOnUiThread(final Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            HANDLER.post(runnable);
        }
    }

    public static void runOnUiThreadDelayed(final Runnable runnable, long delayMillis) {
        HANDLER.postDelayed(runnable, delayMillis);
    }

    /**
     * Executes the given task in a fixed thread pool.
     *
     * @param size The size of thread in the fixed thread pool.
     * @param task The task to execute.
     * @param <T>  The type of the task's result.
     */
    public static <T> void executeByFixed(@IntRange(from = 1) final int size, final Task<T> task) {
        execute(getPool(size, Thread.NORM_PRIORITY, 0, 0), task);
    }

    /**
     * Executes the given task in a fixed thread pool after the given delay.
     *
     * @param size     The size of thread in the fixed thread pool.
     * @param task     The task to execute.
     * @param delay    The time from now to delay execution.
     * @param unit     The time unit of the delay parameter.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
     */
    public static <T> void executeByFixedWithDelay(@IntRange(from = 1) final int size,
                                                   final Task<T> task,
                                                   final long delay,
                                                   final TimeUnit unit,
                                                   @IntRange(from = 1, to = 10) final int priority) {
        executeWithDelay(getPool(size, priority, delay, 0), task, delay, unit);
    }

    /**
     * Executes the given task in a fixed thread pool at fix rate.
     *
     * @param size     The size of thread in the fixed thread pool.
     * @param task     The task to execute.
     * @param period   The period between successive executions.
     * @param unit     The time unit of the period parameter.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
     */
    public static <T> void executeByFixedAtFixRate(@IntRange(from = 1) final int size,
                                                   final Task<T> task,
                                                   final long period,
                                                   final TimeUnit unit,
                                                   @IntRange(from = 1, to = 10) final int priority) {
        executeAtFixedRate(getPool(size, Thread.NORM_PRIORITY, 0, period), task, 0, period, unit);
    }

    /**
     * Executes the given task in a fixed thread pool at fix rate.
     *
     * @param size         The size of thread in the fixed thread pool.
     * @param task         The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period       The period between successive executions.
     * @param unit         The time unit of the initialDelay and period parameters.
     * @param priority     The priority of thread in the poll.
     * @param <T>          The type of the task's result.
     */
    public static <T> void executeByFixedAtFixRate(@IntRange(from = 1) final int size,
                                                   final Task<T> task,
                                                   long initialDelay,
                                                   final long period,
                                                   final TimeUnit unit,
                                                   @IntRange(from = 1, to = 10) final int priority) {
        executeAtFixedRate(getPool(size, priority, initialDelay, period), task, initialDelay, period, unit);
    }

    /**
     * Executes the given task in a single thread pool.
     *
     * @param task     The task to execute.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
     */
    public static <T> void executeBySingle(final Task<T> task,
                                           @IntRange(from = 1, to = 10) final int priority) {
        execute(getPool(TYPE_SINGLE, priority, 0, 0), task);
    }

    /**
     * Executes the given task in a single thread pool after the given delay.
     *
     * @param task     The task to execute.
     * @param delay    The time from now to delay execution.
     * @param unit     The time unit of the delay parameter.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
     */
    public static <T> void executeBySingleWithDelay(final Task<T> task,
                                                    final long delay,
                                                    final TimeUnit unit,
                                                    @IntRange(from = 1, to = 10) final int priority) {
        executeWithDelay(getPool(TYPE_SINGLE, priority, delay, 0), task, delay, unit);
    }

    /**
     * Executes the given task in a single thread pool at fix rate.
     *
     * @param task     The task to execute.
     * @param period   The period between successive executions.
     * @param unit     The time unit of the period parameter.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
     */
    public static <T> void executeBySingleAtFixRate(final Task<T> task,
                                                    final long period,
                                                    final TimeUnit unit,
                                                    @IntRange(from = 1, to = 10) final int priority) {
        executeAtFixedRate(getPool(TYPE_SINGLE, priority, 0, period), task, 0, period, unit);
    }

    /**
     * Executes the given task in a single thread pool at fix rate.
     *
     * @param task         The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period       The period between successive executions.
     * @param unit         The time unit of the initialDelay and period parameters.
     * @param <T>          The type of the task's result.
     */
    public static <T> void executeBySingleAtFixRate(final Task<T> task,
                                                    long initialDelay,
                                                    final long period,
                                                    final TimeUnit unit) {
        executeAtFixedRate(getPool(TYPE_SINGLE, Thread.NORM_PRIORITY, initialDelay, period), task, initialDelay, period, unit);
    }

    /**
     * Executes the given task in a cached thread pool.
     *
     * @param task The task to execute.
     * @param <T>  The type of the task's result.
     */
    public static <T> void executeByCached(final Task<T> task) {
        execute(getPool(TYPE_CACHED, Thread.NORM_PRIORITY, 0, 0), task);
    }

    /**
     * Executes the given task in a cached thread pool after the given delay.
     *
     * @param task  The task to execute.
     * @param delay The time from now to delay execution.
     * @param unit  The time unit of the delay parameter.
     * @param <T>   The type of the task's result.
     */
    public static <T> void executeByCachedWithDelay(final Task<T> task,
                                                    final long delay,
                                                    final TimeUnit unit) {
        executeWithDelay(getPool(TYPE_CACHED, Thread.NORM_PRIORITY, delay, 0), task, delay, unit);
    }

    /**
     * Executes the given task in a cached thread pool at fix rate.
     *
     * @param task   The task to execute.
     * @param period The period between successive executions.
     * @param unit   The time unit of the period parameter.
     * @param <T>    The type of the task's result.
     */
    public static <T> void executeByCachedAtFixRate(final Task<T> task,
                                                    final long period,
                                                    final TimeUnit unit) {
        executeAtFixedRate(getPool(TYPE_CACHED, Thread.NORM_PRIORITY, 0, period), task, 0, period, unit);
    }

    /**
     * Executes the given task in a cached thread pool at fix rate.
     *
     * @param task         The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period       The period between successive executions.
     * @param unit         The time unit of the initialDelay and period parameters.
     * @param <T>          The type of the task's result.
     */
    public static <T> void executeByCachedAtFixRate(final Task<T> task,
                                                    long initialDelay,
                                                    final long period,
                                                    final TimeUnit unit) {
        executeAtFixedRate(getPool(TYPE_CACHED, Thread.NORM_PRIORITY, initialDelay, period), task, initialDelay, period, unit);
    }

    /**
     * Executes the given task in an IO thread pool.
     *
     * @param task The task to execute.
     * @param <T>  The type of the task's result.
     */
    public static <T> void executeByIo(final Task<T> task) {
        execute(getPool(TYPE_IO, Thread.NORM_PRIORITY, 0, 0), task);
    }

    /**
     * Executes the given task in an IO thread pool after the given delay.
     *
     * @param task  The task to execute.
     * @param delay The time from now to delay execution.
     * @param unit  The time unit of the delay parameter.
     * @param <T>   The type of the task's result.
     */
    public static <T> void executeByIoWithDelay(final Task<T> task,
                                                final long delay,
                                                final TimeUnit unit) {
        executeWithDelay(getPool(TYPE_IO, Thread.NORM_PRIORITY, delay, 0), task, delay, unit);
    }

    /**
     * Executes the given task in an IO thread pool at fix rate.
     *
     * @param task   The task to execute.
     * @param period The period between successive executions.
     * @param unit   The time unit of the period parameter.
     * @param <T>    The type of the task's result.
     */
    public static <T> void executeByIoAtFixRate(final Task<T> task,
                                                final long period,
                                                final TimeUnit unit) {
        executeAtFixedRate(getPool(TYPE_IO, Thread.NORM_PRIORITY, 0, period), task, 0, period, unit);
    }

    /**
     * Executes the given task in an IO thread pool at fix rate.
     *
     * @param task         The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period       The period between successive executions.
     * @param unit         The time unit of the initialDelay and period parameters.
     * @param <T>          The type of the task's result.
     */
    public static <T> void executeByIoAtFixRate(final Task<T> task,
                                                long initialDelay,
                                                final long period,
                                                final TimeUnit unit) {
        executeAtFixedRate(getPool(TYPE_IO, Thread.NORM_PRIORITY, initialDelay, period), task, initialDelay, period, unit);
    }

    /**
     * Executes the given task in a cpu thread pool.
     *
     * @param task The task to execute.
     * @param <T>  The type of the task's result.
     */
    public static <T> void executeByCpu(final Task<T> task) {
        execute(getPool(TYPE_CPU, Thread.NORM_PRIORITY, 0, 0), task);
    }

    /**
     * Executes the given task in a cpu thread pool after the given delay.
     *
     * @param task  The task to execute.
     * @param delay The time from now to delay execution.
     * @param unit  The time unit of the delay parameter.
     * @param <T>   The type of the task's result.
     */
    public static <T> void executeByCpuWithDelay(final Task<T> task,
                                                 final long delay,
                                                 final TimeUnit unit) {
        executeWithDelay(getPool(TYPE_CPU, Thread.NORM_PRIORITY, delay, 0), task, delay, unit);
    }

    /**
     * Executes the given task in a cpu thread pool at fix rate.
     *
     * @param task   The task to execute.
     * @param period The period between successive executions.
     * @param unit   The time unit of the period parameter.
     * @param <T>    The type of the task's result.
     */
    public static <T> void executeByCpuAtFixRate(final Task<T> task,
                                                 final long period,
                                                 final TimeUnit unit) {
        executeAtFixedRate(getPool(TYPE_CPU, Thread.NORM_PRIORITY, 0, period), task, 0, period, unit);
    }

    /**
     * Executes the given task in a cpu thread pool at fix rate.
     *
     * @param task         The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period       The period between successive executions.
     * @param unit         The time unit of the initialDelay and period parameters.
     * @param <T>          The type of the task's result.
     */
    public static <T> void executeByCpuAtFixRate(final Task<T> task,
                                                 long initialDelay,
                                                 final long period,
                                                 final TimeUnit unit) {
        executeAtFixedRate(getPool(TYPE_CPU, Thread.NORM_PRIORITY, initialDelay, period), task, initialDelay, period, unit);
    }

    /**
     * Executes the given task in a custom thread pool.
     *
     * @param pool The custom thread pool.
     * @param task The task to execute.
     * @param <T>  The type of the task's result.
     */
    public static <T> void executeByCustom(final ExecutorService pool, final Task<T> task) {
        execute(pool, task);
    }

    /**
     * Executes the given task in a custom thread pool after the given delay.
     *
     * @param pool  The custom thread pool.
     * @param task  The task to execute.
     * @param delay The time from now to delay execution.
     * @param unit  The time unit of the delay parameter.
     * @param <T>   The type of the task's result.
     */
    public static <T> void executeByCustomWithDelay(final ExecutorService pool,
                                                    final Task<T> task,
                                                    final long delay,
                                                    final TimeUnit unit) {
        executeWithDelay(pool, task, delay, unit);
    }

    /**
     * Cancel the given task.
     *
     * @param task The task to cancel.
     */
    public static void cancel(final Task task) {
        if (task == null) return;
        Future<?> future = TASK_FUTURE_MAP.get(task);
        if (future != null && !future.isCancelled()) {
            future.cancel(true);
        }
        task.cancel();
    }

    /**
     * Cancel the given tasks.
     *
     * @param tasks The tasks to cancel.
     */
    public static void cancel(final Task... tasks) {
        if (tasks == null || tasks.length == 0) return;
        for (Task task : tasks) {
            if (task == null) continue;
            cancel(task);
        }
    }

    /**
     * Cancel the given tasks.
     *
     * @param tasks The tasks to cancel.
     */
    public static void cancel(final List<Task> tasks) {
        if (tasks == null || tasks.size() == 0) return;
        for (Task task : tasks) {
            if (task == null) continue;
            cancel(task);
        }
    }

    /**
     * Cancel the tasks in pool.
     *
     * @param executorService The pool.
     */
    public static void cancel(ExecutorService executorService) {
        if (executorService instanceof ThreadPoolExecutor4Util) {
            for (Map.Entry<Task, ExecutorService> taskTaskInfoEntry : TASK_POOL_MAP.entrySet()) {
                if (taskTaskInfoEntry.getValue() == executorService) {
                    cancel(taskTaskInfoEntry.getKey());
                }
            }
        } else {
            Log.e("LogUtils", "The executorService is not ThreadUtils's pool.");
        }
    }

    /**
     * Set the deliver.
     *
     * @param deliver The deliver.
     */
    public static void setDeliver(final Executor deliver) {
        sDeliver = deliver;
    }

    public static void setDelegate(ExecutorService delegate) {
        sDelegate = delegate;
    }

    private static <T> void execute(final ExecutorService pool, final Task<T> task) {
        execute(pool, task, 0, 0, null);
    }

    private static <T> void executeWithDelay(final ExecutorService pool,
                                             final Task<T> task,
                                             final long delay,
                                             final TimeUnit unit) {
        execute(pool, task, delay, 0, unit);
    }

    private static <T> void executeAtFixedRate(final ExecutorService pool,
                                               final Task<T> task,
                                               long delay,
                                               final long period,
                                               final TimeUnit unit) {
        execute(pool, task, delay, period, unit);
    }

    private static <T> void execute(final ExecutorService pool, final Task<T> task,
                                    long delay, final long period, final TimeUnit unit) {
        ExecutorService tempPool = sDelegate != null ? sDelegate : pool;
        synchronized (TASK_POOL_MAP) {
            if (TASK_POOL_MAP.get(task) != null) {
                Log.e("ThreadUtils", "Task can only be executed once.");
                return;
            }
            TASK_POOL_MAP.put(task, tempPool);
        }
        Future<?> future = null;
        if (period == 0) {
            if (delay == 0) {
                future = tempPool.submit(task);
            } else {
                if (tempPool instanceof ScheduledExecutorService) {
                    future = ((ScheduledExecutorService) tempPool).schedule(task, delay, unit);
                }
            }
        } else {
            task.setSchedule(true);
            if (tempPool instanceof ScheduledExecutorService) {
                future = ((ScheduledExecutorService) tempPool).scheduleAtFixedRate(task, delay, period, unit);
            }
        }
        TASK_FUTURE_MAP.put(task, future);
    }

    private static ExecutorService getPoolByTypeAndPriority(final int type, final int priority) {
        synchronized (TYPE_PRIORITY_POOLS) {
            ExecutorService pool;
            Map<Integer, ExecutorService> priorityPools = TYPE_PRIORITY_POOLS.get(type);
            if (priorityPools == null) {
                priorityPools = new ConcurrentHashMap<>();
                pool = ThreadPoolExecutor4Util.createPool(type, priority);
                priorityPools.put(priority, pool);
                TYPE_PRIORITY_POOLS.put(type, priorityPools);
            } else {
                pool = priorityPools.get(priority);
                if (pool == null) {
                    pool = ThreadPoolExecutor4Util.createPool(type, priority);
                    priorityPools.put(priority, pool);
                }
            }
            return pool;
        }
    }

    private static ExecutorService getPool(final int type, final int priority, long delay, long period) {
        if (delay != 0 || period != 0) {
            return getScheduledPool(type);
        } else {
            return getPoolByTypeAndPriority(type, priority);
        }
    }

    private static ScheduledExecutorService getScheduledPool(int type) {
        synchronized (SCHEDULED_POOLS) {
            ScheduledExecutorService pool = SCHEDULED_POOLS.get(type);
            if (pool == null) {
                int corePoolSize;
                switch (type) {
                    case TYPE_SINGLE:
                        corePoolSize = 1;
                        break;

                    case TYPE_CACHED:
                        corePoolSize = 0;
                        break;

                    case TYPE_IO:
                        corePoolSize = 2 * CPU_COUNT + 1;
                        break;

                    case TYPE_CPU:
                        corePoolSize = CPU_COUNT + 1;
                        break;

                    default:
                        corePoolSize = type;
                        break;
                }
                pool = new ScheduledThreadPoolExecutor(corePoolSize);
                SCHEDULED_POOLS.put(type, pool);
            }
            return pool;
        }
    }

    static final class ThreadPoolExecutor4Util extends ThreadPoolExecutor {

        private static ExecutorService createPool(final int type, final int priority) {
            switch (type) {
                case TYPE_SINGLE:
                    return new ThreadPoolExecutor4Util(1, 1,
                            0L, TimeUnit.MILLISECONDS,
                            new LinkedBlockingQueue4Util(),
                            new UtilsThreadFactory("single", priority)
                    );
                case TYPE_CACHED:
                    return new ThreadPoolExecutor4Util(0, 128,
                            60L, TimeUnit.SECONDS,
                            new LinkedBlockingQueue4Util(true),
                            new UtilsThreadFactory("cached", priority)
                    );
                case TYPE_IO:
                    return new ThreadPoolExecutor4Util(2 * CPU_COUNT + 1, 2 * CPU_COUNT + 1,
                            30, TimeUnit.SECONDS,
                            new LinkedBlockingQueue4Util(),
                            new UtilsThreadFactory("io", priority)
                    );
                case TYPE_CPU:
                    return new ThreadPoolExecutor4Util(CPU_COUNT + 1, 2 * CPU_COUNT + 1,
                            30, TimeUnit.SECONDS,
                            new LinkedBlockingQueue4Util(true),
                            new UtilsThreadFactory("cpu", priority)
                    );
                default:
                    return new ThreadPoolExecutor4Util(type, type,
                            0L, TimeUnit.MILLISECONDS,
                            new LinkedBlockingQueue4Util(),
                            new UtilsThreadFactory("fixed(" + type + ")", priority)
                    );
            }
        }

        private final AtomicInteger mSubmittedCount = new AtomicInteger();

        private LinkedBlockingQueue4Util mWorkQueue;

        ThreadPoolExecutor4Util(int corePoolSize, int maximumPoolSize,
                                long keepAliveTime, TimeUnit unit,
                                LinkedBlockingQueue4Util workQueue,
                                ThreadFactory threadFactory) {
            super(corePoolSize, maximumPoolSize,
                    keepAliveTime, unit,
                    workQueue,
                    threadFactory
            );
            workQueue.mPool = this;
            mWorkQueue = workQueue;
        }

        private int getSubmittedCount() {
            return mSubmittedCount.get();
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            mSubmittedCount.decrementAndGet();
            super.afterExecute(r, t);
        }

        @Override
        public void execute(@NonNull Runnable command) {
            if (this.isShutdown()) return;
            mSubmittedCount.incrementAndGet();
            try {
                super.execute(command);
            } catch (RejectedExecutionException ignore) {
                Log.e("ThreadUtils", "This will not happen!");
                mWorkQueue.offer(command);
            } catch (Throwable t) {
                mSubmittedCount.decrementAndGet();
            }
        }
    }

    private static final class LinkedBlockingQueue4Util extends LinkedBlockingQueue<Runnable> {

        private volatile ThreadPoolExecutor4Util mPool;

        private int mCapacity = Integer.MAX_VALUE;

        LinkedBlockingQueue4Util() {
            super();
        }

        LinkedBlockingQueue4Util(boolean isAddSubThreadFirstThenAddQueue) {
            super();
            if (isAddSubThreadFirstThenAddQueue) {
                mCapacity = 0;
            }
        }

        LinkedBlockingQueue4Util(int capacity) {
            super();
            mCapacity = capacity;
        }

        @Override
        public boolean offer(@NonNull Runnable runnable) {
            if (mCapacity <= size() &&
                    mPool != null && mPool.getPoolSize() < mPool.getMaximumPoolSize()) {
                // create a non-core thread
                return false;
            }
            return super.offer(runnable);
        }
    }

    private static final class UtilsThreadFactory extends AtomicLong
            implements ThreadFactory {
        private static final AtomicInteger POOL_NUMBER      = new AtomicInteger(1);
        private static final long          serialVersionUID = -9209200509960368598L;
        private final        String        namePrefix;
        private final        int           priority;
        private final        boolean       isDaemon;

        UtilsThreadFactory(String prefix, int priority) {
            this(prefix, priority, false);
        }

        UtilsThreadFactory(String prefix, int priority, boolean isDaemon) {
            namePrefix = prefix + "-pool-" +
                    POOL_NUMBER.getAndIncrement() +
                    "-thread-";
            this.priority = priority;
            this.isDaemon = isDaemon;
        }

        @Override
        public Thread newThread(@NonNull Runnable r) {
            Thread t = new Thread(r, namePrefix + getAndIncrement()) {
                @Override
                public void run() {
                    try {
                        super.run();
                    } catch (Throwable t) {
                        Log.e("ThreadUtils", "Request threw uncaught throwable", t);
                    }
                }
            };
            t.setDaemon(isDaemon);
            t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    e.printStackTrace();
                }
            });
            t.setPriority(priority);
            return t;
        }
    }

    public abstract static class SimpleTask<T> extends Task<T> {

        @Override
        public void onCancel() {
            Log.e("ThreadUtils", "onCancel: " + Thread.currentThread());
        }

        @Override
        public void onFail(Throwable t) {
            Log.e("ThreadUtils", "onFail: ", t);
        }

    }

    public abstract static class Task<T> implements Runnable {

        private static final int NEW         = 0;
        private static final int RUNNING     = 1;
        private static final int EXCEPTIONAL = 2;
        private static final int COMPLETING  = 3;
        private static final int CANCELLED   = 4;
        private static final int INTERRUPTED = 5;
        private static final int TIMEOUT     = 6;

        private final AtomicInteger state = new AtomicInteger(NEW);

        private volatile boolean isSchedule;
        private volatile Thread  runner;

        private Timer mTimer;

        private Executor deliver;

        public abstract T doInBackground() throws Throwable;

        public abstract void onSuccess(T result);

        public abstract void onCancel();

        public abstract void onFail(Throwable t);

        @Override
        public void run() {
            if (isSchedule) {
                if (runner == null) {
                    if (!state.compareAndSet(NEW, RUNNING)) return;
                    runner = Thread.currentThread();
                } else {
                    if (state.get() != RUNNING) return;
                }
            } else {
                if (!state.compareAndSet(NEW, RUNNING)) return;
                runner = Thread.currentThread();
            }
            try {
                final T result = doInBackground();
                if (isSchedule) {
                    if (state.get() != RUNNING) return;
                    getDeliver().execute(new Runnable() {
                        @Override
                        public void run() {
                            onSuccess(result);
                        }
                    });
                } else {
                    if (!state.compareAndSet(RUNNING, COMPLETING)) return;
                    getDeliver().execute(new Runnable() {
                        @Override
                        public void run() {
                            onSuccess(result);
                            onDone();
                        }
                    });
                }
            } catch (InterruptedException ignore) {
                state.compareAndSet(CANCELLED, INTERRUPTED);
            } catch (final Throwable throwable) {
                if (!state.compareAndSet(RUNNING, EXCEPTIONAL)) return;
                getDeliver().execute(new Runnable() {
                    @Override
                    public void run() {
                        onFail(throwable);
                        onDone();
                    }
                });
            }
        }

        public void cancel() {
            cancel(true);
        }

        public void cancel(boolean mayInterruptIfRunning) {
            synchronized (state) {
                if (state.get() > RUNNING) return;
                state.set(CANCELLED);
            }
            if (mayInterruptIfRunning) {
                if (runner != null) {
                    runner.interrupt();
                }
            }

            getDeliver().execute(new Runnable() {
                @Override
                public void run() {
                    onCancel();
                    onDone();
                }
            });
        }

        private void timeout() {
            synchronized (state) {
                if (state.get() > RUNNING) return;
                state.set(TIMEOUT);
            }
            if (runner != null) {
                runner.interrupt();
            }
            onDone();
        }


        public boolean isCanceled() {
            return state.get() >= CANCELLED;
        }

        public boolean isDone() {
            return state.get() > RUNNING;
        }

        public Task<T> setDeliver(Executor deliver) {
            this.deliver = deliver;
            return this;
        }

        public void setTimeout(final long timeoutMillis, final OnTimeoutListener listener) {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!isDone() && listener != null) {
                        timeout();
                        listener.onTimeout();
                    }
                }
            }, timeoutMillis);
        }

        private void setSchedule(boolean isSchedule) {
            this.isSchedule = isSchedule;
        }

        private Executor getDeliver() {
            if (deliver == null) {
                return getGlobalDeliver();
            }
            return deliver;
        }

        @CallSuper
        protected void onDone() {
            TASK_POOL_MAP.remove(this);
            TASK_FUTURE_MAP.remove(this);
            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }
        }

        public interface OnTimeoutListener {
            void onTimeout();
        }
    }

    private static Executor getGlobalDeliver() {
        if (sDeliver == null) {
            sDeliver = new Executor() {

                @Override
                public void execute(@NonNull Runnable command) {
                    runOnUiThread(command);
                }
            };
        }
        return sDeliver;
    }
}

package slive.nsfly.common.concurrent;

import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 描述：<pre>
 *     线程池相关工具类
 *
 * @author Slive
 * @date 2021/6/14 5:22 下午
 */
public class ThreadPoolUtils {

    private static final Logger log = LoggerFactory.getLogger(ThreadPoolUtils.class);

    private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    private static final Map<String, ThreadPoolExecutor> THREAD_POOL_MAP = new HashMap<>();

    private static final int DEFAULT_CORE_POOL_SIZE = 1;

    private static final int DEFAULT_KEEP_ALIVE_TIME = 15;

    public static ThreadPoolExecutor createExecutorDirectAndSyn(String threadPoolId, int corePoolSize, int maximumPoolSize, int keepAliveTime, TimeUnit timeUnit, long waitContinueMills) {
        return createExecutor(threadPoolId, corePoolSize, maximumPoolSize, DEFAULT_KEEP_ALIVE_TIME, timeUnit, new SynchronousQueue<>(true),
                new SynchronousQueueWaitPolicy(waitContinueMills));
    }

    public static ThreadPoolExecutor createExecutorDirect(String threadPoolId, int corePoolSize, int maximumPoolSize) {
        return createExecutor(threadPoolId, corePoolSize, maximumPoolSize, DEFAULT_KEEP_ALIVE_TIME, null, new SynchronousQueue<>(true), createDefaultRejectedExecutionHandler());
    }

    public static ThreadPoolExecutor createExecutorDirect(String threadPoolId, int corePoolSize, int maximumPoolSize,
                                                          int keepAliveTime) {
        return createExecutor(threadPoolId, corePoolSize, maximumPoolSize, keepAliveTime, null, new SynchronousQueue<>(true), createDefaultRejectedExecutionHandler());
    }

    public static ThreadPoolExecutor createExecutorDirect(String threadPoolId, int corePoolSize, int maximumPoolSize,
                                                          int keepAliveTime, TimeUnit timeUnit) {
        return createExecutor(threadPoolId, corePoolSize, maximumPoolSize, keepAliveTime, timeUnit, new SynchronousQueue<>(true), createDefaultRejectedExecutionHandler());
    }

    /**
     * @param threadPoolId             线程池id
     * @param corePoolSize             主线程池数
     * @param maximumPoolSize          最大线程池数
     * @param keepAliveTime            保留线程时间
     * @param timeUnit                 保留线程时间单位，默认s
     * @param workQueue                线程缓冲队列，默认SynchronousQueue
     * @param rejectedExecutionHandler 拒绝策略
     * @return
     */
    public static ThreadPoolExecutor createExecutor(String threadPoolId, int corePoolSize, int maximumPoolSize,
                                                    int keepAliveTime, TimeUnit timeUnit, BlockingQueue<Runnable> workQueue,
                                                    RejectedExecutionHandler rejectedExecutionHandler) {
        if (threadPoolId == null) {
            throw new RuntimeException("threadPoolId is null.");
        }

        synchronized (THREAD_POOL_MAP) {
            if (THREAD_POOL_MAP.containsKey(threadPoolId)) {
                return THREAD_POOL_MAP.get(threadPoolId);
            }
            log.info("before create thread pool executor, threadPoolId:{}, corePoolSize:{}, maximumPoolSize:{}" +
                            ", keepAliveTime:{}, timeUnit:{}", threadPoolId, corePoolSize,
                    maximumPoolSize, keepAliveTime, timeUnit);

            String threadPrefix = "";
            if (!(threadPoolId.startsWith("T-") || threadPoolId.startsWith("t-"))) {
                threadPrefix = "T-" + threadPoolId;
            } else {
                threadPrefix = threadPoolId;
            }
            if (corePoolSize <= 0) {
                corePoolSize = DEFAULT_CORE_POOL_SIZE;
            }

            if (maximumPoolSize < corePoolSize) {
                maximumPoolSize = corePoolSize;
            }

            if (keepAliveTime <= 0) {
                keepAliveTime = DEFAULT_KEEP_ALIVE_TIME;
            }

            if (timeUnit == null) {
                timeUnit = TimeUnit.SECONDS;
            }

            if (workQueue == null) {
                workQueue = new SynchronousQueue<>(true);
            }

            if (rejectedExecutionHandler == null) {
                rejectedExecutionHandler = createDefaultRejectedExecutionHandler();
            }

            ThreadPoolExecutor poolExecutor = new InnerThreadPoolExecutor(threadPoolId, corePoolSize,
                    maximumPoolSize, keepAliveTime, timeUnit, workQueue, createThreadFactory(threadPrefix), rejectedExecutionHandler);

            log.info("after create thread pool executor, threadPoolId:{}, corePoolSize:{}, maximumPoolSize:{}" +
                    ", keepAliveTime:{}, timeUnit:{}", threadPoolId, corePoolSize, maximumPoolSize, keepAliveTime, timeUnit);
            THREAD_POOL_MAP.put(threadPoolId, poolExecutor);
            return poolExecutor;
        }
    }

    /**
     * 创建线程工厂类
     *
     * @param threadPrefix 线程名前缀
     * @return
     */
    public static ThreadFactory createThreadFactory(String threadPrefix) {
        ThreadFactory tf = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                // 拼接线程名称
                String threadFinalName = getThreadFinalName(threadPrefix);
                t.setName(threadFinalName);
                // 设置为守护线程
                t.setDaemon(true);
                return t;
            }
        };
        return tf;
    }

    private static String getThreadFinalName(String threadPrefix) {
        return (threadPrefix + "-" + System.currentTimeMillis() + RandomUtils.nextInt(0, 9999));
    }

    private static RejectedExecutionHandler createDefaultRejectedExecutionHandler() {
        return new InnerCallerRunsPolicy();
    }

    static class InnerCallerRunsPolicy extends ThreadPoolExecutor.CallerRunsPolicy {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            String executorId = "";
            if (e instanceof InnerThreadPoolExecutor) {
                executorId = ((InnerThreadPoolExecutor) e).getExecutorId();
            }

            if (r instanceof Thread) {
                log.warn("reject callerRun, executorId:{}, name:{}", executorId, r.getClass().getSimpleName());
            } else {
                log.warn("reject callerRun, executorId:{}, hascode:{}", executorId, r.hashCode());
            }

            super.rejectedExecution(r, e);
        }
    }

    static class SynchronousQueueWaitPolicy implements RejectedExecutionHandler {

        // 如果被拒绝，等待时长
        private long waitContinue = 1;

        public SynchronousQueueWaitPolicy(long waitContinue) {
            if (waitContinue <= 0) {
                waitContinue = 1;
            }
            this.waitContinue = waitContinue;
        }

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            String executorId = "";
            if (e instanceof InnerThreadPoolExecutor) {
                executorId = ((InnerThreadPoolExecutor) e).getExecutorId();
            }

            if (r instanceof Thread) {
                log.warn("reject waitcaller, executorId:{}, name:{}", executorId, r.getClass().getSimpleName());
            } else {
                log.warn("reject waitcaller, executorId:{}, hascode:{}", executorId, r.hashCode());
            }

            if (!e.isShutdown()) {
                try {
                    // 正常来说，队列里是为空的，等待一段时间
                    e.getQueue().poll(waitContinue, TimeUnit.MILLISECONDS);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                e.execute(r);
            }
        }
    }

    static class InnerThreadPoolExecutor extends ThreadPoolExecutor {
        private String executorId;

        public InnerThreadPoolExecutor(String executorId, int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
            this.executorId = executorId;
        }

        @Override
        public void execute(Runnable command) {
            try {
                super.execute(command);
            } catch (Exception ex) {
                log.error("execute error.", ex);
            }
        }

        @Override
        protected void afterExecute(Runnable r, Throwable e) {
            if (e != null) {
                if (r instanceof Thread) {
                    log.warn("execute runnable error, executorId:{}, name:{}", executorId, r.getClass().getSimpleName(), e);
                } else {
                    log.warn("execute runnable error, executorId:{}, hascode:{}", executorId, r.hashCode(), e);
                }
            }
            super.afterExecute(r, e);
            if (log.isDebugEnabled()) {
                log.debug("after execute, executorId:{}, hashcode:{}, poolSize:{}, lPoolSize:{}, activeCount:{}, taskCount:{}, cpTaskCount:{}, queueSize:{}",
                        executorId, r.hashCode(), this.getPoolSize(), this.getLargestPoolSize(), this.getActiveCount(),
                        this.getTaskCount(), this.getCompletedTaskCount(), this.getQueue().size());
            }
        }

        public String getExecutorId() {
            return executorId;
        }
    }
}

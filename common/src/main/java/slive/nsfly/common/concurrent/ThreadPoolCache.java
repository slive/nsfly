package slive.nsfly.common.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/7/28 7:11 下午
 */
public class ThreadPoolCache {

    private static final Logger log = LoggerFactory.getLogger(ThreadPoolCache.class);

    private BlockingQueue<Runnable> blockingQueue = null;

    private Executor executor = null;

    private Thread cycleExecute = null;

    private String threadPoolId;

    public ThreadPoolCache(String threadPoolId, int corePoolSize, int maximumPoolSize) {
        init(threadPoolId, corePoolSize, maximumPoolSize, maximumPoolSize * 10, 0, null, 10);
    }

    public ThreadPoolCache(String threadPoolId, int corePoolSize, int maximumPoolSize, int cacheSize, long waitContinueMills) {
        init(threadPoolId, corePoolSize, maximumPoolSize, cacheSize, 0, null, waitContinueMills);
    }

    public ThreadPoolCache(String threadPoolId, int corePoolSize, int maximumPoolSize, int cacheSize, int keepAliveTime, TimeUnit timeUnit, long waitContinueMills) {
        init(threadPoolId, corePoolSize, maximumPoolSize, cacheSize, keepAliveTime, timeUnit, waitContinueMills);
    }

    private void init(String threadPoolId, int corePoolSize, int maximumPoolSize, int cacheSize, int keepAliveTime, TimeUnit timeUnit, long waitContinueMills) {
        if (cacheSize <= 0) {
            cacheSize = maximumPoolSize;
        }
        this.threadPoolId = threadPoolId;
        blockingQueue = new ArrayBlockingQueue<>(cacheSize, true);
        executor = ThreadPoolUtils.createExecutorDirectAndSyn(threadPoolId, corePoolSize, maximumPoolSize, keepAliveTime, timeUnit, waitContinueMills);
        cycleExecute = new InnerCycleExecute();
        cycleExecute.start();
    }

    public boolean submitImmediately(Runnable runnable) {
        if (runnable != null) {
            boolean offer = blockingQueue.offer(runnable);
            if (!offer) {
                if (runnable instanceof Thread) {
                    log.warn("submit runnable fail, name:{}", runnable.getClass().getSimpleName());
                } else {
                    log.warn("submit runnable fail, hashcode:{}", runnable.hashCode());
                }
            }
            return offer;
        }
        return false;
    }

    public boolean submitUntilAvaliable(Runnable runnable) {
        try {
            blockingQueue.put(runnable);
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }

    class InnerCycleExecute extends Thread {
        InnerCycleExecute() {
            String threadName = threadPoolId + "-cycle";
            if (!(threadPoolId.startsWith("T-") || threadPoolId.startsWith("t-"))) {
                threadName = "T-" + threadPoolId;
            }
            setName(threadName);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Runnable runnable = blockingQueue.take();
                    if (runnable != null) {
                        executor.execute(runnable);
                    }
                } catch (Exception ex) {
                    log.warn("cycle execute runnable error.", ex);
                }
            }
        }
    }
}

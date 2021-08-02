package slive.nsfly.common.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/7/29 9:36 上午
 */
public class ThreadPoolCacheTest {

    private static final Logger log = LoggerFactory.getLogger(ThreadPoolCacheTest.class);

    private ThreadPoolCache threadPoolCache = null;

    private int testCount = 100;

    @org.junit.Before
    public void setUp() throws Exception {
        threadPoolCache = new ThreadPoolCache("testpool", 2, 5);
    }

    @org.junit.Test
    public void submitImmediately() {
        CountDownLatch cdl = new CountDownLatch(100);
        for (int index = 0; index < testCount; index++) {
            int runIndex = index;
            boolean b = threadPoolCache.submitUntilAvaliable(new Runnable() {
                @Override
                public void run() {
                    log.info(System.currentTimeMillis() + " success index:" + runIndex);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        cdl.countDown();
                    }
                }
            });
            if (!b) {
                log.info(System.currentTimeMillis() + " fail index:" + runIndex);
                cdl.countDown();
            }
        }
        try {
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("finish...");

    }

    @org.junit.Test
    public void submitUntilAvaliable() {
    }
}
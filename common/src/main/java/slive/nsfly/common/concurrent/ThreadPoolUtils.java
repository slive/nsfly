package slive.nsfly.common.concurrent;

import org.apache.commons.lang3.RandomUtils;

import java.util.concurrent.ThreadFactory;

/**
 * 描述：<pre>
 *     线程池相关工具类
 *
 * @author Slive
 * @date 2021/6/14 5:22 下午
 */
public class ThreadPoolUtils {

    public static ThreadFactory newThreadFactory(String threadPrefix){
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
}

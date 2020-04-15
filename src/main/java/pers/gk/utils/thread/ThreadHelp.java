package pers.gk.utils.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 线程帮助类
 *
 * @author gk
 * @date 2020-04-06
 */
public class ThreadHelp {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadHelp.class);

    public static final String DEFAULT_THREADPOOL = "_DEAULT";
    private static final Map<String, ThreadPoolExecutor> threadPoolsMap = new ConcurrentHashMap<>(16);
    private static final ExecutorService fixedThreadPool = Executors.newFixedThreadPool(20);
    private static final int defaultCoreThreadCount = 75;
    private static final int defaultMaxThreadCount = 249;
    private static final int defaultAppendCoreThreadCount = 30;
    private static final int defaultAppendMaxThreadCount = 200;

    public ThreadHelp() {
        threadPoolsMap.put("_DEAULT", new ThreadPoolExecutor(
                75,
                249,
                60L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10000)));
    }

    public <T extends Callable<String>> void submit(T t, String threadPoolName) {
        Future<String> future;
        if (threadPoolName != null && !"".equals(threadPoolName)) {
            boolean contain = threadPoolsMap.containsKey(threadPoolName);
            if (!contain) {
                synchronized(ThreadHelp.class) {
                    if (!contain) {
                        threadPoolsMap.put(threadPoolName, new ThreadPoolExecutor(
                                30,
                                200,
                                60L,
                                TimeUnit.SECONDS,
                                new ArrayBlockingQueue<>(300000)));
                    }
                }
            }

            future = threadPoolsMap.get(threadPoolName).submit(t);
        } else {
            future = threadPoolsMap.get("_DEAULT").submit(t);
        }

        fixedThreadPool.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                try {
                    future.get(100L, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    LOGGER.error("Thread task InterruptException", e);
                } catch (ExecutionException e) {
                    LOGGER.error("Thread task ExecutionException", e);
                } catch (TimeoutException e) {
                    LOGGER.error("警告：有线程任务执行超过10s！", e);
                }
                return null;
            }
        });
    }

    public void destoryPool() {
        synchronized(threadPoolsMap) {
            for (String key : threadPoolsMap.keySet()) {
                threadPoolsMap.get(key).shutdown();
            }

        }
    }

    public void printThreadPoolInfo() {

        for (Map.Entry<String, ThreadPoolExecutor> entry : threadPoolsMap.entrySet()) {
            String key = entry.getKey();
            ThreadPoolExecutor value = entry.getValue();
            LOGGER.debug("线程池当前使用情况:  poolName={};blockSize={};activeSize={};poolSize={}",
                    key, value.getQueue().size(), value.getActiveCount(), value.getPoolSize());
        }

    }

    public int getQueneSize(String threadPoolName) {
        ThreadPoolExecutor pool = threadPoolsMap.get(threadPoolName);
        return pool.getQueue().size();
    }

    public int getQueneSize() {
        return this.getQueneSize("_DEAULT");
    }
}

package pers.gk.utils.thread;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * 自定义线程帮助类
 * 用于手动创建线程池、提交线程任务
 *
 * @author GK
 * @date 2020-04-06
 */
public class MyThreadHelp {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyThreadHelp.class);

    /**
     * 全局记录线程池的map
     */
    private static final Map<String, ThreadPoolExecutor> THREAD_POOL_EXECUTOR_MAP = new ConcurrentHashMap<>(16);

    /**
     * 线程保活时间
     */
    private static final Long KEEP_ALIVE_TIME = 60L;

    /**
     * 获取返回值超时时间
     */
    private static final long FUTURE_TIEM_OUT = 10L;

    /**
     * 创建线程池，提交线程任务
     *
     * @param t              线程任务
     * @param threadPoolName 线程名 XX-task-%d
     * @param <T>            线程任务
     * @return 线程任务的返回值
     */
    public static <T extends Callable<String>> String submit(T t, String threadPoolName) {
        if (StringUtils.isNotBlank(threadPoolName)) {
            boolean containsKey = THREAD_POOL_EXECUTOR_MAP.containsKey(threadPoolName);
            //双重校验加锁
            if (!containsKey) {
                synchronized (MyThreadHelp.class) {
                    if (!containsKey) {
                        //cpu核数
                        int cpuNum = Runtime.getRuntime().availableProcessors();
                        THREAD_POOL_EXECUTOR_MAP.put(threadPoolName, new ThreadPoolExecutor(
                                cpuNum,
                                cpuNum,
                                KEEP_ALIVE_TIME,
                                TimeUnit.SECONDS,
                                new ArrayBlockingQueue<>(10000),
                                new ThreadFactoryBuilder().setNameFormat(threadPoolName + "-%d").build(),
                                new ThreadPoolExecutor.AbortPolicy()));
                    }
                }
            }
            ThreadPoolExecutor threadPoolExecutor = THREAD_POOL_EXECUTOR_MAP.get(threadPoolName);
            Future<String> stringFuture = threadPoolExecutor.submit(t);
            String result = "";
            try {
                //延迟10s，获取线程任务的结果
                result = stringFuture.get(FUTURE_TIEM_OUT, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                LOGGER.error("Thread interruptException >>", e);
            } catch (ExecutionException e) {
                LOGGER.error("Thread ExecutionException >>", e);
            } catch (TimeoutException e) {
                LOGGER.error("Thread TimeoutException >>", e);
            }
            return result;
        } else {
            LOGGER.error("Thread pool names [{}] are not canonical, build failed!", threadPoolName);
            return null;
        }
    }

    /**
     * 销毁线程池
     *
     * @param threadPoolName 线程池名
     */
    public static void destoryThreadPool(String threadPoolName) {
        if (StringUtils.isNotBlank(threadPoolName)) {
            ThreadPoolExecutor threadPoolExecutor = THREAD_POOL_EXECUTOR_MAP.get(threadPoolName);
            threadPoolExecutor.shutdown();
            THREAD_POOL_EXECUTOR_MAP.remove(threadPoolName);
            LOGGER.debug("Thread Pool [{}] has bean destroyed!");
        } else {
            LOGGER.error("Thread pool names [{}] are not canonical, destroy failed!", threadPoolName);
        }
    }

    /**
     * 测试
     * @param args 参数
     */
    public static void main(String[] args) {
        LongAdder longAdder = new LongAdder();
        longAdder.add(0L);

        AtomicInteger atomicInteger = new AtomicInteger(0);
        String myThreadPoolName = "MyThreadPool-task-0";

        long startMillis = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            String result = MyThreadHelp.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    /*int i = atomicInteger.addAndGet(1);
                    return String.format("[%s] >>AtomicInteger=%s", Thread.currentThread().getName(), i);*/

                    longAdder.increment();
                    return String.format("[%s] >>LongAdder=%s", Thread.currentThread().getName(), longAdder.longValue());
                }
            }, myThreadPoolName);
            LOGGER.debug(result);
        }
        long endMillis = System.currentTimeMillis();
        LOGGER.debug("time-consuming:[{}]", (endMillis - startMillis));

        ThreadPoolExecutor threadPoolExecutor = MyThreadHelp.THREAD_POOL_EXECUTOR_MAP.get(myThreadPoolName);
        LOGGER.debug(threadPoolExecutor.toString());
        MyThreadHelp.destoryThreadPool(myThreadPoolName);
    }
}

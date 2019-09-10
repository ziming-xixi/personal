package pers.gk.utils.ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FTPClientPool extends Object
        implements ObjectPool<FTPClient> {
    private static Logger logger = LoggerFactory.getLogger(FTPClientPool.class);


    private static final int DEFAULT_POOL_SIZE = 5;


    private final AtomicInteger validSize;


    private final BlockingQueue<FTPClient> pool;


    private final FtpClientFactory factory;


    public FTPClientPool(FtpClientFactory factory) {
        this(5, factory);
    }


    public FTPClientPool(int poolSize, FtpClientFactory factory) {
        this.validSize = new AtomicInteger(0);
        this.factory = factory;
        this.pool = new ArrayBlockingQueue(poolSize);
        initPool(poolSize);
        checkPoolState();
    }


    private void initPool(int maxPoolSize) {
        for (int i = 0; i < maxPoolSize; i++) {
            addObject();
        }
    }


    public FTPClient borrowObject() {
        FTPClient client = null;
        try {
            if (this.pool.size() == 0) {
                logger.info("当前连接池中已无FTP连接，需重新创建连接：" + this.pool.size());

                addObject();
            }
            client = (FTPClient) this.pool.poll(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("", e);
        } catch (Exception e) {
            logger.error("", e);
        }
        logger.debug("borrow后ftp连接数为：" + this.pool.size());
        return client;
    }


    public void returnObject(FTPClient client) {
        try {
            if (client != null && !this.pool.offer(client, 3L, TimeUnit.SECONDS)) {
                this.validSize.decrementAndGet();
                this.factory.destroyObject(client);
                logger.error("FTPClient放回连接池失败！");
            }
        } catch (InterruptedException e) {
            logger.error("", e);
        } catch (Exception e) {
            logger.error("", e);
        }

        logger.info("return后ftp连接数为：" + this.pool.size());
    }


    public void invalidateObject(FTPClient client) {
        if (client != null && this.pool.remove(client)) {
            this.validSize.decrementAndGet();
            this.factory.destroyObject(client);
        }
    }

    private void checkPoolState() {
        logger.info("时间：" + new Date() + "，开始线程池状态检查！每两分钟检查一次！");
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        service.scheduleAtFixedRate(new PoolState(null), 0L, 2L, TimeUnit.MINUTES);
    }

    private class PoolState implements Runnable {
        private PoolState(Object o) {
        }

        public void run() {
            try {
                logger.warn("---线程池状态检查---");
                Object[] clients = FTPClientPool.this.pool.toArray();
                for (Object client : clients) {
                    if (client instanceof FTPClient) {
                        FTPClient ftpClient = (FTPClient) client;
                        if (!FTPClientPool.this.factory.validateObject(ftpClient)) {
                            FTPClientPool.this.factory.destroyObject(ftpClient);
                            FTPClientPool.this.invalidateObject(ftpClient);
                        }
                    }
                }

                for (int i = FTPClientPool.this.validSize.get(); i < 5; i++) {
                    FTPClientPool.this.addObject();
                }
            } catch (Exception e) {
                logger.error("线程池状态检查异常", e);
            }
        }
    }


    public void addObject() {
        try {
            FTPClient ftpclient = this.factory.makeObject();
            if (ftpclient != null && this.pool.offer(ftpclient, 3L, TimeUnit.SECONDS)) {
                this.validSize.addAndGet(1);
            }
        } catch (InterruptedException e) {
            logger.error("", e);
        } catch (Exception e) {
            logger.error("", e);
        }
    }


    public int getNumIdle() throws UnsupportedOperationException {
        return 0;
    }


    public int getNumActive() throws UnsupportedOperationException {
        return 0;
    }


    public void clear() {
    }


    public void close() throws InterruptedException {
        while (this.pool.iterator().hasNext()) {
            FTPClient client = (FTPClient) this.pool.take();
            this.factory.destroyObject(client);
        }
    }

    public void setFactory(PoolableObjectFactory<FTPClient> factory) throws IllegalStateException, UnsupportedOperationException {
    }
}


package pers.gk.utils.ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class FTPClientUtil {
    private static final Logger logger = LoggerFactory.getLogger(FTPClientUtil.class);

    private static final String DEFAULT_PASSIVEMODE = "false";

    private static final String DEFAULT_ENCODING = "UTF-8";

    private static final int DEFAULT_CLIENTTIMEOUT = 60000;

    private static final int DEFAULT_THREADNUM = 3;

    private static final int DEFAULT_TRANSFERFILETYPE = 2;

    private static final boolean DEFAULT_RENAMEUPLOADED = false;

    private static final int DEFAULT_RETRYTIMES = 3;

    private static FTPClientUtil ftpClientUtil = null;

    private FTPClientPool ftpClientPool = null;

    private long maxTime = 0L;


    public long getMaxTime() {
        return this.maxTime;
    }


    public void setMaxTime(long maxTime) {
        this.maxTime = maxTime;
    }

    private FTPClientUtil(String host, int port, String userName, String password, String path) {
        this(host, port, userName, password, "false", "UTF-8", 60000, 3, 2, false, 3, path);
    }


    private FTPClientUtil(String host, int port, String userName, String password, String passiveMode, String encoding, int clientTimeout, int threadNum, int transferFileType, boolean renameUploaded, int retryTimes, String path) {
        FTPClientConfigure config = new FTPClientConfigure(host, port, userName, password, passiveMode, encoding, clientTimeout, threadNum, transferFileType, renameUploaded, retryTimes, path);

        this.ftpClientPool = new FTPClientPool(new FtpClientFactory(config));
    }


    public static FTPClientUtil getInstance(String host, int port, String userName, String password, String path) {
        if (ftpClientUtil == null) {
            ftpClientUtil = new FTPClientUtil(host, port, userName, password, path);
        }
        return ftpClientUtil;
    }


    public List<FTPFile> listAllFtpFileByTime(String path, Long updateTime, Long rangeTime) throws Exception {
        FTPClient ftpClient = this.ftpClientPool.borrowObject();
        List<FTPFile> ftpList = new ArrayList<FTPFile>();
        FTPFile[] ftpFileList = ftpClient.listFiles(path);
        if (ftpFileList == null || ftpFileList.length == 0) {
            return null;
        }
        for (FTPFile file : ftpFileList) {
            if (file.isDirectory()) {
                ftpClient.changeWorkingDirectory(path);
                List<FTPFile> list = listAllFtpFileByTime(path + file.getName() + "/", updateTime, rangeTime);
                if (list != null && list.size() > 0) {
                    ftpList.addAll(list);
                }
            } else {
                long fileTime = file.getTimestamp().getTime().getTime();
                this.logger.info("fileTime:" + fileTime + ",updateTime:" + updateTime + ",rangeTime:" + rangeTime);
                this.logger.info("fileTime-updateTime = " + (fileTime - updateTime.longValue()));
                if (fileTime - updateTime.longValue() < rangeTime.longValue() && fileTime - updateTime.longValue() >= 0L) {
                    ftpList.add(file);
                }
                if (fileTime > getMaxTime()) {
                    setMaxTime(fileTime);
                }
            }
        }
        this.ftpClientPool.returnObject(ftpClient);
        return ftpList;
    }


    public FTPClient getFTPClient() {
        return this.ftpClientPool.borrowObject();
    }


    public void returnFTPClient(FTPClient ftpClient) {
        this.ftpClientPool.returnObject(ftpClient);
    }


    public void desFTPClient(FTPClient ftpClient) {
        this.ftpClientPool.returnObject(ftpClient);
    }


    public void invalidateObject(FTPClient ftpClient) {
        this.ftpClientPool.invalidateObject(ftpClient);
    }
}

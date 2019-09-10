package pers.gk.utils.ftp;

public class FTPClientConfigure {
    private String host;
    private int port;
    private String username;
    private String password;
    private String passiveMode;
    private String encoding;
    private int clientTimeout;
    private int threadNum;
    private int transferFileType;
    private boolean renameUploaded;
    private int retryTimes;
    private String path;

    public FTPClientConfigure(String host, int port, String username, String password, String passiveMode, String encoding, int clientTimeout, int threadNum, int transferFileType, boolean renameUploaded, int retryTimes, String path) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.passiveMode = passiveMode;
        this.encoding = encoding;
        this.clientTimeout = clientTimeout;
        this.threadNum = threadNum;
        this.transferFileType = transferFileType;
        this.renameUploaded = renameUploaded;
        this.retryTimes = retryTimes;
        this.path = path;
    }


    public String getHost() {
        return this.host;
    }


    public void setHost(String host) {
        this.host = host;
    }


    public int getPort() {
        return this.port;
    }


    public void setPort(int port) {
        this.port = port;
    }


    public String getUsername() {
        return this.username;
    }


    public void setUsername(String username) {
        this.username = username;
    }


    public String getPassword() {
        return this.password;
    }


    public void setPassword(String password) {
        this.password = password;
    }


    public String getPassiveMode() {
        return this.passiveMode;
    }


    public void setPassiveMode(String passiveMode) {
        this.passiveMode = passiveMode;
    }


    public String getEncoding() {
        return this.encoding;
    }


    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }


    public int getClientTimeout() {
        return this.clientTimeout;
    }


    public void setClientTimeout(int clientTimeout) {
        this.clientTimeout = clientTimeout;
    }


    public int getThreadNum() {
        return this.threadNum;
    }


    public void setThreadNum(int threadNum) {
        this.threadNum = threadNum;
    }


    public int getTransferFileType() {
        return this.transferFileType;
    }


    public void setTransferFileType(int transferFileType) {
        this.transferFileType = transferFileType;
    }


    public boolean isRenameUploaded() {
        return this.renameUploaded;
    }


    public void setRenameUploaded(boolean renameUploaded) {
        this.renameUploaded = renameUploaded;
    }


    public int getRetryTimes() {
        return this.retryTimes;
    }


    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }


    public String getPath() {
        return this.path;
    }


    public void setPath(String path) {
        this.path = path;
    }
}

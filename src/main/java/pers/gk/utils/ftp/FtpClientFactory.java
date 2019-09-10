package pers.gk.utils.ftp;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.pool.PoolableObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class FtpClientFactory extends Object implements PoolableObjectFactory<FTPClient> {
    private static Logger logger;
    private FTPClientConfigure config;

    public FtpClientFactory(FTPClientConfigure config) {
        this.logger = LoggerFactory.getLogger(FtpClientFactory.class);





        this.config = config;
    }







    public FTPClient makeObject() {
        FTPClient ftpClient = new FTPClient();
        ftpClient.setDefaultTimeout(this.config.getClientTimeout());
        ftpClient.setConnectTimeout(this.config.getClientTimeout());
        try {
            ftpClient.connect(this.config.getHost(), this.config.getPort());
            int reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                this.logger.warn("FTPServer refused connection");
                return null;
            }
            boolean result = ftpClient.login(this.config.getUsername(), this.config.getPassword());
            if (!result) {
                throw new FTPClientException("ftpClient��������! userName:" + this.config.getUsername() + " ; password:" + this.config
                        .getPassword());
            }
            ftpClient.setFileType(this.config.getTransferFileType());
            ftpClient.setBufferSize(1024);
            ftpClient.setDataTimeout(this.config.getClientTimeout());
            ftpClient.setSoTimeout(this.config.getClientTimeout());
            ftpClient.setControlEncoding(this.config.getEncoding());
            if (this.config.getPassiveMode().equals("true")) {
                ftpClient.enterLocalPassiveMode();
            }
            String path = this.config.getPath();
            if (StringUtils.isNotBlank(path)) {
                path = new String(path.getBytes("gbk"), "iso-8859-1");
                boolean flag = ftpClient.makeDirectory(path);
                if (!flag) {
                    makeDirectorys(path, ftpClient);
                }
                ftpClient.changeWorkingDirectory(path);
            }
            return ftpClient;
        } catch (IOException e) {
            this.logger.error("", e);
        } catch (FTPClientException e) {
            this.logger.error("", e);
        } catch (Exception e) {
            this.logger.error("", e);
        }
        return null;
    }








    private void makeDirectorys(String path, FTPClient ftpClient) throws IOException {
        String[] paths = path.split("/");
        if (paths != null && paths.length > 1) {
            String s = "";
            for (int i = 0; i < paths.length; i++) {
                if (paths[i] != null && paths[i].length() != 0) {
                    s = s + "/" + paths[i];
                    ftpClient.makeDirectory(s);
                }
            }
        }
    }










    public void destroyObject(FTPClient ftpClient) {
        try {
            if (ftpClient != null && ftpClient.isConnected()) {
                ftpClient.logout();
            }
        } catch (IOException e) {
            this.logger.error("", e);
        } finally {

            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                this.logger.error("", e);
            }
        }
    }









    public boolean validateObject(FTPClient ftpClient) {
        try {
            return ftpClient.sendNoOp();
        } catch (IOException e) {
            this.logger.error("", e);

            return false;
        }
    }

    public void activateObject(FTPClient ftpClient) {}

    public void passivateObject(FTPClient ftpClient) {}
}


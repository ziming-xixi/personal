package pers.gk.utils.ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * apache.commons.net.ftp.FTPClient的使用及详解
 *
 * @author GK
 * @date 2020-04-12
 */
public class FTPClientDemo {
    private static final Logger LOGGER = LoggerFactory.getLogger(FTPClientDemo.class);

    /**
     * FTP服务器ip
     */
    private String host;
    /**
     * FTP服务器端口
     */
    private Integer port;
    /**
     * FTP服务器登录用户名
     */
    private String userName;
    /**
     * FTP服务器登录密码
     */
    private String password;
    /**
     * 目录，必须是绝对路径
     * [/]表示FTP服务器的根目录
     */
    private String path;
    /**
     * 本地编码
     */
    private String localCharset = "GBK";
    /**
     * FTP服务器编码，FTP协议规定的编码
     */
    private String serverCharset = "ISO-8859-1";

    public FTPClientDemo(String host, Integer port, String userName, String password, String path) {
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.path = path;
    }

    public FTPClient getFTPClient() {
        //创建FTPClient对象
        FTPClient ftpClient = new FTPClient();
        //连接FTP服务器
        //ftpClient.connect(host, port);

        return null;
    }
}

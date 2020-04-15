package pers.gk.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.gk.utils.ftp.FTPClientUtil;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class Test {
    private static final Logger LOGGER = LoggerFactory.getLogger(Test.class);

    private String host = "127.0.0.1";
    private int port = 21;
    private String userName = "admin";
    private String password = "123456";
    private String ftpPath = "/";
    private String localCharset = "GBK";
    private String serverCharset = "ISO-8859-1";

    @org.junit.Test
    public void ftpTest() {


        FTPClientUtil ftpClientUtil = FTPClientUtil.getInstance(host, port, userName, password, ftpPath);
        FTPClient ftpClient = ftpClientUtil.getFTPClient();
        boolean makeDirectorySuccess = false;
        try {
            String filePath = "测试2" + File.separatorChar + "测试3";

            //ftp解决目录和文件名乱码
            //开启服务器对UTF-8的支持，如果服务器支持则使用UTF-8，否则使用本地编码GBK
            if (FTPReply.isPositiveCompletion(ftpClient.sendCommand("OPTS UTF8", "ON"))) {
                localCharset = "UTF-8";
            }
            ftpClient.setControlEncoding(localCharset);
            //将路径使用本地编码编码，在使用ISO-8859-1解码
            filePath = new String(filePath.getBytes(localCharset), serverCharset);
            System.out.println(filePath);

            makeDirectorySuccess = ftpClient.makeDirectory(filePath);
            ftpClientUtil.returnFTPClient(ftpClient);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(makeDirectorySuccess);
    }

    @org.junit.Test
    public void bcpTest() throws IOException {
        String host = "127.0.0.1";
        String port = "21";
        String userName = "admin";
        String password = "123456";
        String path = "/";
        FTPClientUtil ftpClientUtil = FTPClientUtil.getInstance(host, Integer.valueOf(port), userName, password, path);
        FTPClient ftpClient = ftpClientUtil.getFTPClient();
        ftpClient.setControlEncoding("GBK");
        FTPFile[] ftpFiles = ftpClient.listFiles();

        InputStream is = null;
        BufferedReader br = null;
        for (FTPFile ftpFile : ftpFiles) {
            StringBuffer sb = new StringBuffer();

            if (!ftpFile.isDirectory()) {
                //String name = new String(ftpFiles[i].getName().getBytes("GBK"), "GBK");
                String name = ftpFile.getName();
                System.out.println(name);
                is = ftpClient.retrieveFileStream(new String(name.getBytes("UTF-8"), "ISO-8859-1"));
                String line = "";
                br = new BufferedReader(new InputStreamReader(is));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }


                System.out.println(sb.toString());
            }
        }

        br.close();
        is.close();

        ftpClientUtil.returnFTPClient(ftpClient);

    }

    @org.junit.Test
    public void urlTest() {
        String url = "ftp://90437@127.0.0.1:8003/中国/西安/大华";
        try { 
            //编码
            String encode = URLEncoder.encode(url, "UTF-8");
            System.out.println(encode);

            //解码
            String decode = URLDecoder.decode(encode, "UTF-8");
            System.out.println(decode);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 逐级创建多级目录
     * 从ftpPath开始逐级创建目录
     */
    @org.junit.Test
    public void ftpMakeDirectory() throws IOException {
        FTPClientUtil ftpClientUtil = FTPClientUtil.getInstance(host, port, userName, password, ftpPath);
        FTPClient ftpClient = ftpClientUtil.getFTPClient();
        if (FTPReply.isPositiveCompletion(ftpClient.sendCommand("OPTS UTF8", "ON"))) {
            localCharset = "UTF-8";
        }
        //当前路径。FTPClient已切换至当前路径下，从当前路径下开始逐级创建目录
        String path = "/测试1/测试2/测试3/测试4";
        String currentPath = "";
        String[] pathSplit = path.split("/");
        for (String directory : pathSplit) {
            if (StringUtils.isBlank(directory)) {
                continue;
            }

            /*
             * 切换至要创建的目录下，
             * 若切换成功，说明要创建的目录已存在，则不创建
             * 若切换失败，说明要创建的目录不存在，则创建
             */
            currentPath = currentPath + "/" + directory;
            String encodeCurrentPath = new String(currentPath.getBytes(localCharset), serverCharset);
            boolean makeDirectorySuccess = ftpClient.makeDirectory(encodeCurrentPath);
            if (makeDirectorySuccess) {
                LOGGER.debug("make directory [{}] successful！", currentPath);
            }
            /*boolean changeWorkingDirectorySuccess = ftpClient.changeWorkingDirectory(encodeCurrentPath);
            if (!changeWorkingDirectorySuccess) {
                String encodeDirectory = new String(directory.getBytes(localCharset), serverCharset);
                //创建目录
                boolean makeDirectorySuccess = ftpClient.makeDirectory(encodeDirectory);
                if (makeDirectorySuccess) {
                    LOGGER.debug("make directory [{}] successful！", directory);
                    //切换至该目录下
                    boolean changeSuccess = ftpClient.changeWorkingDirectory(encodeCurrentPath);
                    if (changeSuccess) {
                        String workingDirectory = ftpClient.printWorkingDirectory();
                        workingDirectory = new String(workingDirectory.getBytes(serverCharset), localCharset);
                        LOGGER.debug("change working directory [{}] successful!", workingDirectory);
                    } else {
                        String workingDirectory = ftpClient.printWorkingDirectory();
                        workingDirectory = new String(workingDirectory.getBytes(serverCharset), localCharset);
                        LOGGER.error("change working directory [{}] failed! current working directoty is [{}]",
                                directory, workingDirectory);
                    }
                } else {
                    LOGGER.error("make directory [{}] failed！", directory);
                }
            } else {
                LOGGER.debug("directory [{}] is existed!", directory);
            }*/
        }
        ftpClientUtil.returnFTPClient(ftpClient);
    }

    /**
     * 指定目录或文件是否存在
     */
    @org.junit.Test
    public void directotyOrFileIsExist() throws IOException {
        FTPClientUtil ftpClientUtil = FTPClientUtil.getInstance(host, port, userName, password, ftpPath);
        FTPClient ftpClient = ftpClientUtil.getFTPClient();
        if (FTPReply.isPositiveCompletion(ftpClient.sendCommand("OPTS UTF8", "ON"))) {
            localCharset = "UTF-8";
        }
        String path = "测试1/测试2/测试4.txt";
        String parentPath = path.substring(0, path.lastIndexOf("/"));
        String fileName = path.substring(path.lastIndexOf("/") + 1);
        String encodeParentPath = new String(parentPath.getBytes(localCharset), serverCharset);
        FTPFile[] ftpFiles = ftpClient.listFiles(encodeParentPath);
        for (FTPFile ftpFile : ftpFiles) {
            if (fileName.equals(ftpFile.getName())) {
                LOGGER.debug("[{}] is exist", fileName);
            } else {
                LOGGER.debug("[{}] is not exist", fileName);
            }
        }
        ftpClientUtil.returnFTPClient(ftpClient);
    }


}

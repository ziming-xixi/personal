package pers.gk.utils.Base64;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Base64工具类
 *
 * @author gk
 * @date 2019-09-09
 * @version 1.0
 */
public class Base64Util {

    private static final Logger LOGGER = LoggerFactory.getLogger(Base64Util.class);

    /**
     * 根据文件url，获取文件的Base64字符串
     * @param fileUrl 源文件url
     * @return Base64字符串
     */
    public static String getFileBase64(String fileUrl) {
        if (StringUtils.isBlank(fileUrl)) {
            return "";
        }

        //存放原文件的byte[]
        byte[] bytes = null;
        //源文件转换的Base64字符串
        String base64Content = null;
        //UTF-8编码后的url
        String encodeUrl = "";

        try {
            encodeUrl = URLEncoder.encode(fileUrl, "UTF-8");
            encodeUrl = encodeUrl.replace("%2F", "/");
            encodeUrl = encodeUrl.replace("%3A", ":");
            URL url = new URL(encodeUrl);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setConnectTimeout(10000);
            urlConnection.setReadTimeout(10000);
            InputStream is = urlConnection.getInputStream();
            ByteArrayOutputStream byteOS = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int len = 0;
            while ((len = is.read(buff)) != -1) {
                byteOS.write(buff, 0, len);
            }
            //byte输出流将文件存入byte[]中
            bytes = byteOS.toByteArray();
            base64Content = Base64.encodeBase64String(bytes);

            //关流
            byteOS.close();
            is.close();
        } catch (UnsupportedEncodingException e) {
            LOGGER.error(fileUrl + "UTF-8编码异常，获取文件Base64格式失败！返回空字符串！", e);
        } catch (MalformedURLException e) {
            LOGGER.error("UTF-8编码后的" + encodeUrl + "获取URL对象异常！", e);
        } catch (IOException e) {
            LOGGER.error("UTF-8编码后的" + encodeUrl + "获取连接异常！", e);
        }
        return base64Content;
    }


}

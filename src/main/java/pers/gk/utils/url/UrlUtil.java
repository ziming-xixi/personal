package pers.gk.utils.url;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 文件路径工具类
 *
 * @author gk
 * @date 2019-09-09
 * @version 1.0
 */
public class UrlUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(UrlUtil.class);

    /**
     * 文件url映射为可访问url，如：/aaa/bbb/c.jpg映射为：http:127.0.0.1:8080/aaa/bbb/c.jpg
     * @param fileUrl 以"/"开头的url
     * @param mapIPAndPort 映射ip和por，格式：http://ip:port
     * @return 可访问的url
     */
    public static String mapIPAndPort(String fileUrl, String mapIPAndPort) {
        String newFileUrl = "";

        try {
            if (StringUtils.isNotBlank(fileUrl) && StringUtils.isNotBlank(mapIPAndPort)) {
                if (fileUrl.startsWith("http") || fileUrl.startsWith("ftp")) {
                    newFileUrl = fileUrl;
                    LOGGER.info("原文件url以http或ftp开头，返回原文件url：" + fileUrl);
                }

                if (fileUrl.startsWith("/")) {
                    newFileUrl = mapIPAndPort + fileUrl;
                    LOGGER.info("原文件url以\"/\"开头，映射后的文件url：" + newFileUrl);
                }

            } else {
                LOGGER.info("文件url或映射IP：port为空，返回空路径！");
            }
        } catch (Exception e) {
            LOGGER.error(fileUrl + "---" + mapIPAndPort + "映射转换异常，返回空url！", e);
        }
        return newFileUrl;
    }
}

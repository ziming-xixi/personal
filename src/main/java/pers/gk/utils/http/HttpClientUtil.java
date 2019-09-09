package pers.gk.utils.http;


import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * HttpClient工具类
 * 基于org.apache.httpcomponents.httpclient的4.5.9版本
 *
 * @author gk
 * @version 1.0
 * @date 2019-09-06
 */
public class HttpClientUtil {
    private static Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

    private static CloseableHttpClient httpClient = null;

    private static RequestConfig requestConfig = RequestConfig.custom()
            .setConnectionRequestTimeout(30000) //从PoolingHttpClientConnectionManager(连接池)获取连接超时时间，单位毫秒。
            .setConnectTimeout(30000) //连接超时时间，单位毫秒
            .setSocketTimeout(30000) //响应超时时间，单位毫秒
            .setRedirectsEnabled(false) //不允许重定向---因无法获取重定向url
            .build();


    private static HttpClient getHttpClient() {
        if (httpClient == null) {
            initHttpClient();
        }
        return httpClient;
    }


    private static void initHttpClient() {
        if (httpClient == null) {
            //协议http对应的处理socket链接工厂
            PlainConnectionSocketFactory plainConnectionSocketFactory = PlainConnectionSocketFactory.getSocketFactory();
            //协议https对应的处理socket链接工厂
            SSLConnectionSocketFactory sSLConnectionSocketFactory = SSLConnectionSocketFactory.getSocketFactory();

            // 设置协议http和https对应的处理socket链接工厂的对象
            Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", plainConnectionSocketFactory).register("https", sSLConnectionSocketFactory).build();

            //设置连接管理器
            PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);

            //以下两个参数涉及并发使用，根据场景谨慎设置
            //MaxTotal(500)设置整个连接池最大连接数 根据自己的场景决定
            cm.setMaxTotal(500);
            /*DefaultMaxPerRoute(200)是路由的默认最大连接（该值默认为2），限制数量实际使用DefaultMaxPerRoute并非MaxTotal。
              设置过小无法支持大并发(ConnectionPoolTimeoutException: Timeout waiting for connection from pool)，路由是对maxTotal的细分。
              用例解释，比如：MaxtTotal=400 DefaultMaxPerRoute=200
                            而我只连接到https://www.baidu.com时，到这个主机的并发最多只有200；而不是400；
                            而我连接到https://www.baidu.com/ 和 http://qq.com时，到每个主机的并发最多只有200；
                            即加起来是400（但不能超过400）；所以起作用的设置是DefaultMaxPerRoute。
             */
            cm.setDefaultMaxPerRoute(200);

            //httpclient在当发生异常的时候，如果重试次数大于3，则重连结束
            HttpRequestRetryHandler retryHandler = (e, i, httpContext) -> {
                if (i > 3) {
                    return false;
                }
                if (e instanceof org.apache.commons.httpclient.NoHttpResponseException) //NoHttpResponseException是一种可以重试的异常，return true则打印下异常日志
                    return true;
                if (e instanceof org.apache.http.conn.ConnectTimeoutException) //ConnectTimeoutException是一种可以重试的异常，return true则打印下异常日志
                    return true;
                if (e instanceof java.net.SocketTimeoutException) //SocketTimeoutException是一种可以重试的异常，return true则打印下异常日志
                    return true;
                if (e instanceof javax.net.ssl.SSLHandshakeException) //SSLHandshakeException是一种不可以重试的异常，return false则抛出异常
                    return false;
                if (e instanceof java.io.InterruptedIOException) //InterruptedIOException是一种不可以重试的异常，return false则抛出异常
                    return false;
                if (e instanceof java.net.UnknownHostException) //UnknownHostException是一种不可以重试的异常，return false则抛出异常
                    return false;
                if (e instanceof javax.net.ssl.SSLException) {
                    return false;
                }
                logger.error("未记录的请求异常>>>>>>" + e.getClass());

                HttpRequest request = HttpClientContext.adapt(httpContext).getRequest();
                if (!(request instanceof org.apache.http.HttpEntityEnclosingRequest)) {
                    return true;
                }
                return false;
            };

            //设置连接管理器、设置重试机制，创建HttpClient对象
            httpClient = HttpClients.custom().setConnectionManager(cm).setRetryHandler(retryHandler).build();
        }
    }

    /**
     * Get请求，数据格式json
     *
     * @param url 请求路径，如：http://10.80.56.196:8090/login?username=aaa&password=123
     * @return 返回json串
     * @throws Exception 异常
     */
    /*public static String executeGET(String url) throws Exception {
        return executeGET(url, null);
    }*/

    /**
     * Get请求，数据格式json
     *
     * @param url     请求路径，如：http://10.80.56.196:8090/login?username=aaa&password=123
     * @param headers 自定义请求头，请求头参数名一致，则默认值；请求头参数名不一致，则新增请求头参数
     * @return 返回json串
     * @throws Exception 异常
     */
    public static String executeGET(String url, List<Header> headers) throws Exception {
        HttpGet get = new HttpGet(url);
        return execute(get, headers);
    }

    /**
     * post请求，数据格式表单
     * @param url 请求路径
     * @param params 请求参数
     * @return 返回字符串
     * @throws Exception 异常
     */
    /*public static String executePOST(String url, List<NameValuePair> params) throws Exception {
        BasicHeader basicHeader = new BasicHeader("Content-Type", "application/x-www-form-urlencoded");
        List<Header> headers = Arrays.asList(new Header[]{basicHeader});
        return executePOST(url, params, headers);
    }*/

    /**
     * post请求，数据格式表单,post表单请求专用
     * Header header = new BasicHeader("Content-Type", "application/x-www-form-urlencoded");
     * NameValuePair nameValuePair = new BasicNameValuePair("name", "tom");
     *
     * @param url     请求路径
     * @param params  请求参数
     * @param headers 自定义请求头，请求头参数名一致，则默认值；请求头参数名不一致，则新增请求头参数
     * @return 返回字符串
     * @throws Exception 异常
     */
    public static String executePOSTByForm(String url, List<NameValuePair> params, List<Header> headers) throws Exception {
        HttpPost post = new HttpPost(url);
        if (CollectionUtils.isNotEmpty(params)) {
            UrlEncodedFormEntity uefEntity = new UrlEncodedFormEntity(params, "UTF-8");
            post.setEntity(uefEntity);
        }
        return execute(post, headers);
    }


    /*public static String executePOSTRequestBody(String url, String param) throws Exception {
        return executePOSTByJson(url, param, null);
    }*/

    /**
     * post请求，数据格式Json
     *
     * @param url 请求路径
     * @param param 请求参数，json格式
     * @param headers 自定义请求头，请求头参数名一致，则默认值；请求头参数名不一致，则新增请求头参数
     * @return 返回Json串
     * @throws IOException 异常
     */
    public static String executePOSTByJson(String url, String param, List<Header> headers) throws IOException {
        HttpPost post = new HttpPost(url);
        if (StringUtils.isNotBlank(param)) {
            post.setEntity(new StringEntity(param, "UTF-8"));
        }
        return execute(post, headers);
    }

    /**
     * HttpClient请求执行核心方法，基础方法，通用方法
     * 默认数据格式：json
     *
     * @param httpMethod 请求方式
     * @param headers    自定义请求头，请求头参数名一致，则默认值；请求头参数名不一致，则新增请求头参数
     * @return 返回json串
     * @throws IOException 异常
     */
    private static String execute(HttpRequestBase httpMethod, List<Header> headers) throws IOException {
        String resultMsg = "";
        logger.info("请求的url" + httpMethod.getURI());
        try {
            httpMethod.setConfig(requestConfig);
            //指定客户端能够接收的内容类型,默认json，默认编码格式UTF-8
            httpMethod.addHeader("Accept", "application/json;charset=UTF-8");
            //请求数据格式，默认json，默认编码格式UTF-8
            httpMethod.addHeader("Content-Type", "application/json;charset=UTF-8");
            //自定义请求头，请求头参数名一致，则默认值；请求头参数名不一致，则新增请求头参数
            if (headers != null && !headers.isEmpty()) {
                for (Header header : headers) {
                    if ("Content-Type".equals(header.getName())) {
                        httpMethod.removeHeaders("Content-Type");
                    }
                    httpMethod.addHeader(header);
                }
            }
            //发送请求
            HttpResponse response = getHttpClient().execute(httpMethod);
            HttpEntity entity = response.getEntity();
            int statusCode = response.getStatusLine().getStatusCode();
            logger.info("[status code]:" + statusCode);
            //重定向不做处理
            /*if (entity != null && statusCode == 302) {
                //重定向处理
            }*/
            //响应成功，用字节输出流写出响应结果
            if (entity != null && statusCode == 200) {
                InputStream in = entity.getContent();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] b = new byte[1024];
                int len;
                while ((len = in.read(b)) > 0) {
                    out.write(b, 0, len);
                }
                resultMsg = out.toString("UTF-8");

                //关流
                out.close();
                in.close();
            } else {
                httpMethod.abort();
            }
        } catch (ClientProtocolException e) {
            throw new ClientProtocolException(e);
        } finally {
            //释放连接
            httpMethod.releaseConnection();
        }
        return resultMsg;
    }

    public static void main(String[] args) {
        String url = "https://www.baidu.com";
        try {
            //Get请求
            String response = executeGET(url, null);
            System.out.println(response);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}

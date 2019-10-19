package com.fenquen.rdelay.utils;

import com.fenquen.rdelay.exception.HttpStatusNot200Exception;
import com.fenquen.rdelay.exception.HttpRespReadException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpUtils {
    private static final Logger LOGGER = Logger.getLogger(HttpUtils.class.getName());

    public static Boolean USE_ASYNC = false;

    private static CloseableHttpClient HTTP_CLIENT_SYNC;

    private static CloseableHttpAsyncClient HTTP_CLIENT_ASYNC;

    static {
        try {
            HTTP_CLIENT_SYNC = HttpClientBuilder.create().build();
            HTTP_CLIENT_ASYNC = buildAsyncHttpClient();
        } catch (Exception e) {
            LOGGER.log(Level.INFO, e.getMessage(), e);
        }
    }

    public static String postStringContentSync(String url, String stringContent)
            throws HttpRespReadException, IOException, HttpStatusNot200Exception {
        HttpPost httpPost = new HttpPost(url);

        httpPost.addHeader("content-type", "application/json;charset=UTF-8");
        httpPost.setEntity(new StringEntity(stringContent));

        HttpResponse httpResponse = HTTP_CLIENT_SYNC.execute(httpPost);

        int code = httpResponse.getStatusLine().getStatusCode();
        if (code != HttpStatus.SC_OK) {
            throw new HttpStatusNot200Exception("HttpStatus:" + code);
        }

        String respStr;
        try {
            respStr = EntityUtils.toString(httpResponse.getEntity());
        } catch (IOException e) {
            throw new HttpRespReadException(e);
        }

        return respStr;
    }


    public static Future<HttpResponse> postStringContentAsync(String url,
                                                              String stringContent,
                                                              FutureCallback<HttpResponse> futureCallback)
            throws UnsupportedEncodingException {

        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("content-type", "application/json;charset=UTF-8");

        httpPost.setEntity(new StringEntity(stringContent));


        return HTTP_CLIENT_ASYNC.execute(httpPost, futureCallback);
    }

    private static CloseableHttpAsyncClient buildAsyncHttpClient() throws Exception {
        IOReactorConfig ioReactorConfig = IOReactorConfig.custom().
                setIoThreadCount(Runtime.getRuntime().availableProcessors())
                .setSoKeepAlive(true)
                .build();

        ConnectingIOReactor connectingIOReactor = new DefaultConnectingIOReactor(ioReactorConfig);

        PoolingNHttpClientConnectionManager connManager = new PoolingNHttpClientConnectionManager(connectingIOReactor);
        connManager.setMaxTotal(100);
        connManager.setDefaultMaxPerRoute(100);

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(60000)
                .setSocketTimeout(60000)
                // get connection from pool
                .setConnectionRequestTimeout(1000)
                .build();

        CloseableHttpAsyncClient closeableHttpAsyncClient = HttpAsyncClients.custom().
                setConnectionManager(connManager)
                .setDefaultRequestConfig(requestConfig).setThreadFactory(runnable -> {
                    Thread thread = new Thread(runnable);
                    thread.setDaemon(true);
                    thread.setName("ASYNC_HTTP_THREAD_" + thread.getId());
                    return thread;
                })
                .build();

        closeableHttpAsyncClient.start();

        return closeableHttpAsyncClient;
    }

    public static void destroy() {
        try {
            HTTP_CLIENT_SYNC.close();
        } catch (IOException e) {
            // no op
        }

        try {
            HTTP_CLIENT_ASYNC.close();
        } catch (IOException e) {
            // no op
        }
    }
}

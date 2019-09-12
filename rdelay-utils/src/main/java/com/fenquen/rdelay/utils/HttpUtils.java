package com.fenquen.rdelay.utils;

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
import java.util.concurrent.TimeUnit;

public class HttpUtils {
    public static Boolean USE_ASYNC = false;

    private static final CloseableHttpClient HTTP_CLIENT_SYNC = HttpClientBuilder.create().build();

    private static CloseableHttpAsyncClient HTTP_CLIENT_ASYNC;

    static {
        try {
            HTTP_CLIENT_ASYNC = buildHttpClientAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String postStringContentSync(String url, String stringContent) throws Exception {
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("content-type", "application/json;charset=UTF-8");
        httpPost.setEntity(new StringEntity(stringContent));

        HttpResponse httpResponse = HTTP_CLIENT_SYNC.execute(httpPost);

        int code = httpResponse.getStatusLine().getStatusCode();
        if (code != HttpStatus.SC_OK) {
            throw new RuntimeException("HttpStatus:" + code);
        }

        return EntityUtils.toString(httpResponse.getEntity());
    }


    public static Future<HttpResponse> postStringContentAsync(String url,
                                                              String stringContent,
                                                              FutureCallback<HttpResponse> futureCallback) {
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("content-type", "application/json;charset=UTF-8");
        try {
            httpPost.setEntity(new StringEntity(stringContent));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        return HTTP_CLIENT_ASYNC.execute(httpPost, futureCallback);
    }

    private static CloseableHttpAsyncClient buildHttpClientAsync() throws Exception {
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
}

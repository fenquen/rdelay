package com.fenquen.rdelay.utils;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class HttpUtils {
    private static final CloseableHttpClient HTTP_CLIENT = HttpClientBuilder.create().build();

    public static String postStringContent(String url, String stringContent) throws Exception {
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("content-type", "application/json;charset=UTF-8");
        httpPost.setEntity(new StringEntity(stringContent));

        HttpResponse httpResponse = HTTP_CLIENT.execute(httpPost);

        int code = httpResponse.getStatusLine().getStatusCode();
        if (code != HttpStatus.SC_OK) {
            throw new RuntimeException("HttpStatus:" + code);
        }

        return EntityUtils.toString(httpResponse.getEntity());
    }


}

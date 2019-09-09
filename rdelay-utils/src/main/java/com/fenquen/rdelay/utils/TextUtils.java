package com.fenquen.rdelay.utils;

import java.util.regex.Pattern;

public class TextUtils {
    private static final Pattern HTTP_SVR_ADDR_PATTERN = Pattern.compile("(http|https)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+");

    public static String verifyAndModifyHttpSvrAddr(String httpSvrAddr) {
        if (!HTTP_SVR_ADDR_PATTERN.matcher(httpSvrAddr).matches()) {
            throw new RuntimeException("httpSvrAddr pattern incorrect,should be like http(s)://host[[/]|[:port[/]]]");
        }

        return httpSvrAddr.substring(0, httpSvrAddr.indexOf("/", httpSvrAddr.indexOf("//")));
    }
}

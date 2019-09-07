package com.fenquen.rdelay.model.common;

public class HttpResp {
    public static final HttpResp PASS = new HttpResp(true, "");
    public Boolean success;
    public String message;

    private HttpResp(Boolean success, String errMsg) {
        this.success = success;
        this.message = errMsg;
    }

    public static HttpResp fail(String message) {
        return new HttpResp(false, message);
    }

    public static HttpResp success(String message){
        return new HttpResp(true, message);
    }
}

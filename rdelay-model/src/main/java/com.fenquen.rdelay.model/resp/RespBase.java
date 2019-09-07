package com.fenquen.rdelay.model.resp;

public class RespBase {
    public Boolean success;

    public String errMsg;

    public RespBase success() {
        success = true;
        successInternal();
        return this;
    }

    public RespBase fail(Throwable throwable) {
        success = false;
        errMsg = throwable.getMessage();
        failInternal(throwable);
        return this;
    }

    protected void successInternal() {

    }

    protected void failInternal(Throwable throwable) {

    }
}

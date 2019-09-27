package com.fenquen.rdelay.model.resp;

public class RespBase {
    public Boolean success;

    public String errClazzName;

    public String errMsg;

    public RespBase success() {
        success = true;
        successInternal();
        return this;
    }

    public RespBase fail(Throwable throwable) {
        success = false;
        errClazzName = throwable.getClass().getSimpleName();
        errMsg = throwable.getMessage();
        failInternal(throwable);
        return this;
    }

    void successInternal() {

    }

    void failInternal(Throwable throwable) {

    }

}

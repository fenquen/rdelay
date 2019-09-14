package com.fenquen.rdelay.model.resp;

import com.fenquen.rdelay.model.Persistence;

public class RespBase  {
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

    void successInternal() {

    }

    void failInternal(Throwable throwable) {

    }

}

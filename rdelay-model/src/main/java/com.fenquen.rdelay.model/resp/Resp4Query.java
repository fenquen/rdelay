package com.fenquen.rdelay.model.resp;

import com.fenquen.rdelay.model.task.TaskBase;

public class Resp4Query extends RespBase {
    public Object data;

    @Override
    void successInternal() {

    }

    @Override
    void failInternal(Throwable throwable) {
    }
}

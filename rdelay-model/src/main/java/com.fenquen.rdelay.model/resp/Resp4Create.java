package com.fenquen.rdelay.model.resp;

public class Resp4Create extends RespBase {

    /**
     * 生成的task的id
     */
    public String id;

    public Resp4Create(String id) {
        this.id = id;
    }

    public Resp4Create() {
    }

    @Override
    protected void successInternal() {

    }

    @Override
    protected void failInternal(Throwable throwable) {

    }
}

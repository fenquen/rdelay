package com.fenquen.rdelay.model.resp;

public class Resp4CreateTask extends RespBase {

    /**
     * 生成的task的id
     */
    public String id;

    public Resp4CreateTask(String id) {
        this.id = id;
    }

    public Resp4CreateTask() {
    }

    @Override
    protected void successInternal() {

    }

    @Override
    protected void failInternal(Throwable throwable) {

    }

}

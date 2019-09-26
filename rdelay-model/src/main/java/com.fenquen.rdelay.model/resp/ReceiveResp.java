package com.fenquen.rdelay.model.resp;

public class ReceiveResp extends RespBase {
    public String taskid;

    public long arrivalTime;

    public long receiveTime;

    @Override
    public void failInternal(Throwable throwable) {
    }

    @Override
    protected void successInternal() {
    }
}

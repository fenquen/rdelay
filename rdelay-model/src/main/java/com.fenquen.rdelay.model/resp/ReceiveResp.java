package com.fenquen.rdelay.model.resp;

import com.fenquen.rdelay.model.task.TaskBase;

public class ReceiveResp extends RespBase {
    public static final long UNKNOWN_TIME = -1;

    public String taskid;

    public String taskName;

    public Long arrivalTime;

    public Long receiveTime;

    @Override
    void failInternal(Throwable throwable) {
    }

    @Override
    void successInternal() {
        receiveTime = System.currentTimeMillis();
    }

    public ReceiveResp() {

    }

    public ReceiveResp(TaskBase taskBase) {
        arrivalTime = System.currentTimeMillis();
        taskid = taskBase.taskid;
        taskName = taskBase.name;
    }
}

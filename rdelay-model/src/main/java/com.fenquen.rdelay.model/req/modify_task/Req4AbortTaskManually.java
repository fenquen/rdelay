package com.fenquen.rdelay.model.req.modify_task;

public class Req4AbortTaskManually extends Req4ModifyTaskState {
    public Req4AbortTaskManually(String taskid) {
        this.taskId = taskid;
    }

    @Override
    public String getRequestUri() {
        return "/abortTaskManually";
    }

    @Override
    void verifyFieldsInternal() {

    }
}

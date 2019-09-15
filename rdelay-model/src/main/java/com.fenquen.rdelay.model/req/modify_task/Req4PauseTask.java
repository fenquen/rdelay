package com.fenquen.rdelay.model.req.modify_task;

public class Req4PauseTask extends Req4ModifyTaskState {
    public Req4PauseTask(String taskid) {
        this.taskId = taskid;
    }
    @Override
    public String getRequestUri() {
        return "/pauseTask";
    }

    @Override
    void verifyFieldsInternal() {

    }
}

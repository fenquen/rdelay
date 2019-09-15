package com.fenquen.rdelay.model.req.modify_task;

public class Req4ResumeTask extends Req4ModifyTaskState {
    public Req4ResumeTask() {

    }

    public Req4ResumeTask(String taskid) {
        this.taskId = taskid;
    }

    @Override
    public String getRequestUri() {
        return "/resumeTask";
    }

    @Override
    void verifyFieldsInternal() {

    }
}

package com.fenquen.rdelay.model.req.modify_task;

import com.fenquen.rdelay.model.req.ReqBase;
import com.fenquen.rdelay.model.task.TaskBase;

public class Req4AbortTaskManually extends Req4ModifyTask {
    @Override
    public String getRequestUri() {
        return "/abortTaskManually";
    }

    @Override
    void verifyFieldsInternal() {

    }
}

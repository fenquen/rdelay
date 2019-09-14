package com.fenquen.rdelay.model.req.modify_task;

import com.fenquen.rdelay.model.req.ReqBase;

public class Req4PauseTask extends Req4ModifyTask {

    @Override
    public String getRequestUri() {
        return "/pauseTask";
    }

    @Override
    void verifyFieldsInternal() {

    }
}

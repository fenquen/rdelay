package com.fenquen.rdelay.model.req.modify_task;

import com.fenquen.rdelay.model.req.ReqBase;

public class Req4ResumeTask extends Req4ModifyTask {


    @Override
    public String getRequestUri() {
        return "/resumeTask";
    }

    @Override
    void verifyFieldsInternal() {

    }
}

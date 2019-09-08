package com.fenquen.rdelay.model.req.create_task;

public class Req4CreateStrContentTask extends Req4CreateTask {
    // meaningful only when taskType is STR_CONTENT
    public String content;

    @Override
    void verifyFieldsInternal() {
        if ("".equals(content.trim())) {
            throw new RuntimeException("");
        }
    }
}

package com.fenquen.rdelay.model.req.create_task;

import com.fenquen.rdelay.model.TaskType;

public class Req4CreateStrContentTask extends Req4CreateTask {
    // meaningful only when taskType is STR_CONTENT
    public String content;

    @Override
    public TaskType getTaskType() {
        return TaskType.STR_CONTENT;
    }

    @Override
    void verifyFieldsInternal() {
        if ("".equals(content.trim())) {
            throw new RuntimeException("");
        }
    }

    public static final String URL = TaskType.STR_CONTENT.name();
}

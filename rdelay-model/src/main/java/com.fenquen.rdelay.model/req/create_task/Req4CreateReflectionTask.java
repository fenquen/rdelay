package com.fenquen.rdelay.model.req.create_task;

import com.fenquen.rdelay.model.task.TaskType;
import com.fenquen.rdelay.model.annotation.Nullable;

public final class Req4CreateReflectionTask extends Req4CreateTask {
    // below fields are meaningful only when taskType is REFLECT
    public String className;

    public String methodName;

    @Nullable
    public String[] paramTypeNames;

    @Nullable
    public String[] params;

    @Override
    public TaskType getTaskType() {
        return TaskType.REFLECT;
    }

    @Override
    void verifyFieldsInternal() {
        boolean paramTypeNamesNull = paramTypeNames == null;
        boolean paramsNull = params == null;

        if (paramsNull != paramTypeNamesNull) {
            throw new RuntimeException();
        }

        if (paramsNull) {
            return;
        }

        if (paramTypeNames.length != params.length) {
            throw new RuntimeException();
        }
    }

    public static final String URL = TaskType.REFLECT.name();

    @Override
    public String getRequestUri() {
        return "/createTask/" + getTaskType().name();
    }
}

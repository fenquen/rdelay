package com.fenquen.rdelay.model.req.create_task;

import com.fenquen.rdelay.model.TaskType;
import com.fenquen.rdelay.model.annotation.Nullable;
import com.fenquen.rdelay.utils.TextUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

/**
 * request for create a task
 */
public abstract class Req4CreateTask {

    public TaskType taskType;

    @Nullable
    public String bizTag;

    public Long executionTime;

    public Integer maxRetryCount = 3;

    public String executionAddr;

    public void verifyFields() throws Exception {
        Field[] fields = getClass().getFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Nullable.class)) {
                continue;
            }

            field.setAccessible(true);
            if (null == field.get(this)) {
                throw new RuntimeException(field.getName() + "is null");
            }
        }

        TextUtils.verifyAndModifyHttpSvrAddr(executionAddr);

        if (System.currentTimeMillis() >= executionTime) {
            throw new RuntimeException("not a valid executionTime,System.currentTimeMillis() >= executionTime");
        }

        verifyFieldsInternal();
    }

    abstract void verifyFieldsInternal();
}

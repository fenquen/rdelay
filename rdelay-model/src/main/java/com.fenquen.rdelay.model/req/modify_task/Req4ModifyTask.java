package com.fenquen.rdelay.model.req.modify_task;

import com.fenquen.rdelay.model.annotation.Nullable;
import com.fenquen.rdelay.model.req.ReqBase;

import java.lang.reflect.Field;

public abstract class Req4ModifyTask extends ReqBase {

    /**
     * TaskBase.taskid
     */
    public String taskId;

    @Override
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
    }

    abstract void verifyFieldsInternal();
}

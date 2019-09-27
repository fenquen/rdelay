package com.fenquen.rdelay.model.req.create_task;

import com.fenquen.rdelay.model.req.ReqBase;
import com.fenquen.rdelay.model.task.TaskType;
import com.fenquen.rdelay.model.annotation.Nullable;
import com.fenquen.rdelay.utils.TextUtils;

import java.lang.reflect.Field;

/**
 * request for create a task
 */
public abstract class Req4CreateTask extends ReqBase {
    public String name;


    @Nullable
    public String description;

    @Nullable
    public String bizTag;

    public Boolean enableCron = false;

    @Nullable
    public Long executionTime;

    @Nullable
    public String cronExpression;


    public Integer maxRetryCount = 3;

    /**
     * the application server address where this task is desired to be executed <br>
     * the field  only should be like http(s)://host[[/]|[:port[/]]]
     */
    public String executionAppSvrAddr;

    private String taskReceiveUrl;

    // used only in internal part,should not invoked by user
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

            if (String.class.equals(field.getType())) {
                if ("".equals(field.get(this))) {
                    throw new RuntimeException(field.getName() + "is string is empty");
                }
            }
        }

        executionAppSvrAddr = TextUtils.verifyAndModifyHttpSvrAddr(executionAppSvrAddr);
        taskReceiveUrl = executionAppSvrAddr + "/rdelay/receiveTask/" + getTaskType().name();

        // use cron or not
        if (enableCron) {
            if (null == cronExpression || "".equals(cronExpression.trim())) {
                throw new RuntimeException("cronExpression is virtually empty when cron enabled");
            }
        } else {
            if (null == executionTime || System.currentTimeMillis() >= executionTime) {
                throw new RuntimeException("not a valid executionTime,System.currentTimeMillis() >= executionTime");
            }

        }

        verifyFieldsInternal();
    }

    public abstract TaskType getTaskType();

    abstract void verifyFieldsInternal();

    public String getTaskReceiveUrl() {
        return taskReceiveUrl;
    }
}

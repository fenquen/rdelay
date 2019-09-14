package com.fenquen.rdelay.model.task;

import com.fenquen.rdelay.model.ModelBase;
import com.fenquen.rdelay.model.annotation.Nullable;

/**
 * a model describing timing task
 */
public class TaskBase extends ModelBase {

    /**
     * unique tag to be distinguished by
     */
    public String taskid;

    public String name;

    public String description;

    /**
     * business tag
     */
    public String bizTag;

    public TaskType taskType;

    public Boolean enableCron;

    /**
     * desired unix timestamp ms when the task is executed
     */
    public long executionTime;

    public String cronExpression;

    public int maxRetryCount;

    public int retriedCount;

    public String taskReceiveUrl;

    public long createTime;

    public final String getMyClazzName() {
        return getClass().getName();
    }

    @Override
    public DbMetaData getDbMetaData() {
        return DbMetaData.TASK;
    }
}



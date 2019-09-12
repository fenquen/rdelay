package com.fenquen.rdelay.model.task;

import com.fenquen.rdelay.model.ModelBase;

/**
 * a model describing timing task
 */
public abstract class AbstractTask extends ModelBase {
    /**
     * unique tag to be distinguished by
     */
    public String id;

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
    public ModelType getModel() {
        return ModelType.TASK;
    }
}



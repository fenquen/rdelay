package com.fenquen.rdelay.model.task;

import com.fenquen.rdelay.model.Persistence;

/**
 * a model describing timing task
 */
public class TaskBase implements Persistence {

    /**
     * unique tag to be distinguished by
     * pattern: taskType@uuid
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

    /**
     * the url of the controller which receives this task
     */
    public String taskReceiveUrl;

    public long createTime;

    public TaskState taskState;

    public Long versionNum;

    public final String getMyClazzName() {
        return getClass().getName();
    }

    @Override
    public DbMetaData getDbMetaData() {
        return DbMetaData.TASK;
    }

    /**
     * describe the lifecycle of a task
     */
    public enum TaskState {
        NORMAL,
        PAUSED,
        ABORTED_MANUALLY,
        COMPLETED_NORMALLY,
        ABORTED_WITH_TOO_MANY_RETRIES
    }


}



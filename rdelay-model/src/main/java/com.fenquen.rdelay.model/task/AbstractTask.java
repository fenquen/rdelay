package com.fenquen.rdelay.model.task;

import com.fenquen.rdelay.model.TaskType;

/**
 * a model describing timing task
 */
public abstract class AbstractTask {
    /**
     * unique tag to be distinguished
     */
    public String id;

    /**
     * business tag
     */
    public String bizTag;


    public TaskType taskType;

    /**
     * desired unix timestamp ms when the task is executed
     */
    public long executionTime;


    public int maxRetryCount;


    public int retriedCount;

    /**
     * the application where this task is desired to be executed
     * the field  only should be like http://host[[/]|[:port[/]]]
     */
    public String executionAddr;

    public long createTime;

    public final String getMyClazzName() {
        return getClass().getName();
    }
}



package com.fenquen.rdelay.model.req;

import com.fenquen.rdelay.model.TaskType;

/**
 * request for create a task
 */
public class Req4CreateTask {

    public TaskType taskType;

    public String bizTag;

    public long executionTime;

    public int maxRetryCount;

    public String executionAddr;

    // meaningful only when taskType is STR_CONTENT
    public String content;

    // below fields are meaningful only when taskType is REFLECT
    public String className;

    public String methodName;

    public String[] paramTypeNames;

    public String[] params;

}

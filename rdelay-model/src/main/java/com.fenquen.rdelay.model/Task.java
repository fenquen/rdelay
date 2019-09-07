package com.fenquen.rdelay.model;

import com.fenquen.rdelay.model.req.Req4CreateTask;

import java.util.Date;
import java.util.UUID;

/**
 * a model describing timing task
 */
public class Task {
    /**
     * unique tag to be distinguished
     */
    public String id;

    /**
     * business tag
     */
    public String bizTag;

    /**
     * desired timestamp when the task is executed
     */
    public long executionTime;


    public int maxRetryCount;


    public int retriedCount;

    /**
     * the application where this task is desired to be executed
     * the field  only should be like http://host:port
     */
    public String executionAddr;

    /**
     * usually a json string
     */
    public String content;


    public long createTime;


    public static Task buildTaskByReq4Create(Req4CreateTask req4Create) {
        Task task = new Task();

        long now = new Date().getTime();
        task.id = UUID.randomUUID().toString();
        task.bizTag = req4Create.bizTag;
        task.executionTime = now + req4Create.delay;
        task.maxRetryCount = req4Create.maxRetryCount;
        task.executionAddr = req4Create.executionAddr;
        task.content = req4Create.content;
        task.createTime = now;

        return task;
    }
}



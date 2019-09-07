package com.fenquen.rdelay.model;

import com.fenquen.rdelay.model.req.Req4Create;

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
    public long executeTime;


    public int maxRetryCount;


    public int retriedCount;

    /**
     * the address where the task is sent when executeTime is reached i.e. timeup
     */
    public String TimeupBackAddr;

    /**
     * usually a json string
     */
    public String content;


    public long createTime;


    public static Task buildTaskByReq4Create(Req4Create req4Create) {
        Task task = new Task();

        long now = new Date().getTime();
        task.id = UUID.randomUUID().toString();
        task.bizTag = req4Create.bizTag;
        task.executeTime = now + req4Create.delay;
        task.maxRetryCount = req4Create.maxRetryCount;
        task.TimeupBackAddr = req4Create.TimeupBackAddr;
        task.content = req4Create.content;
        task.createTime = now;

        return task;
    }
}



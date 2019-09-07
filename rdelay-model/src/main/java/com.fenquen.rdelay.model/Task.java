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

    /**
     * usually a json string
     */
    public String content;


    public long createTime;



}



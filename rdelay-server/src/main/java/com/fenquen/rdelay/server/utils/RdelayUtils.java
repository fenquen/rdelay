package com.fenquen.rdelay.server.utils;

import com.alibaba.fastjson.JSON;
import com.fenquen.rdelay.model.task.TaskBase;
import com.fenquen.rdelay.model.task.TaskType;

public abstract class RdelayUtils {
    public static TaskBase parseTask(String taskid, String taskJsonStr) {
        String taskTypeStr = taskid.substring(0, taskid.indexOf("@"));
        TaskType taskType = TaskType.valueOf(taskTypeStr);

        TaskBase task = null;
        switch (taskType) {
            case REFLECTION:
                task = JSON.parseObject(taskJsonStr, taskType.clazz);
                break;
            case STR_CONTENT:
                task = JSON.parseObject(taskJsonStr, taskType.clazz);
                break;
        }

        return task;
    }
}

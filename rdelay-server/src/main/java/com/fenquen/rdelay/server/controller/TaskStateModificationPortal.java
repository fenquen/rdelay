package com.fenquen.rdelay.server.controller;

import com.alibaba.fastjson.JSON;
import com.fenquen.rdelay.model.req.modify_task.Req4AbortTaskManually;
import com.fenquen.rdelay.model.req.modify_task.Req4PauseTask;
import com.fenquen.rdelay.model.req.modify_task.Req4ResumeTask;
import com.fenquen.rdelay.model.resp.RespBase;
import com.fenquen.rdelay.model.task.TaskBase;
import com.fenquen.rdelay.model.task.TaskType;
import com.fenquen.rdelay.server.redis.RedisOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TaskStateModificationPortal {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskStateModificationPortal.class);

    @Autowired
    private RedisOperator redisOperator;

    @Value("${rdelay.dashboard.enabled}")
    private Boolean dashBoardEnabled;

    @Value("${rdelay.dashboard.topic.name}")
    private String dashboardTopicName;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @RequestMapping(value = "/abortTaskManually", method = RequestMethod.POST)
    public RespBase abortTaskManually(@RequestBody Req4AbortTaskManually req4AbortTaskManually) {
        RespBase respBase = new RespBase();
        try {
            String result = redisOperator.abortTaskManually(req4AbortTaskManually.taskId);
            if (null == result) {
                throw new RuntimeException("maybe the task is already already " +
                        "ABORTED_MANUALLY, COMPLETED_NORMALLY or ABORTED_WITH_TOO_MANY_RETRIES");
            }
            postProcess(req4AbortTaskManually.taskId, TaskBase.TaskState.ABORTED_MANUALLY, result);
            respBase.success();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            respBase.fail(e);
        }
        return respBase;
    }

    @RequestMapping(value = "/pauseTask", method = RequestMethod.POST)
    public RespBase pauseTask(@RequestBody Req4PauseTask req4PauseTask) {
        RespBase respBase = new RespBase();
        try {
            String result = redisOperator.pauseTask(req4PauseTask.taskId);
            if (null == result) {
                throw new RuntimeException("maybe the task is already already " +
                        "PAUSED,ABORTED_MANUALLY,COMPLETED_NORMALLY or ABORTED_WITH_TOO_MANY_RETRIES");
            }
            postProcess(req4PauseTask.taskId, TaskBase.TaskState.PAUSED, result);
            respBase.success();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            respBase.fail(e);
        }
        return respBase;
    }

    @RequestMapping(value = "/resumeTask", method = RequestMethod.POST)
    public RespBase resumeTask(@RequestBody Req4ResumeTask req4ResumeTask) {
        RespBase respBase = new RespBase();
        try {
            String result = redisOperator.resumeTask(req4ResumeTask.taskId);
            if (null == result) {
                throw new RuntimeException("maybe the task is not paused now");
            }
            postProcess(req4ResumeTask.taskId, TaskBase.TaskState.NORMAL, result);
            respBase.success();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            respBase.fail(e);
        }
        return respBase;
    }

    private void postProcess(String taskid, TaskBase.TaskState taskState, String luaResult) {
        if (!dashBoardEnabled) {
            return;
        }

        String[] arr = luaResult.split("&");

        String jsonStr = arr[0];
        Long versionNum = Long.valueOf(arr[1]);

        //String jsonStr = redisOperator.getTaskJsonStr(taskId);

        // taskid pattern taskType@uuid
        String taskTypeStr = taskid.split("@")[0];
        TaskType taskType = TaskType.valueOf(taskTypeStr);

        // modify taskState
        TaskBase taskBase = JSON.parseObject(jsonStr, taskType.clazz);
        taskBase.taskState = taskState;
        taskBase.versionNum = versionNum;

        kafkaTemplate.send(dashboardTopicName, taskBase.getDbMetaData().name(), JSON.toJSONString(taskBase));
    }
}

package com.fenquen.rdelay.server.controller;

import com.alibaba.fastjson.JSON;
import com.fenquen.rdelay.model.req.modify_task.Req4PauseTask;
import com.fenquen.rdelay.model.req.modify_task.Req4ResumeTask;
import com.fenquen.rdelay.model.req.create_task.Req4CreateReflectionTask;
import com.fenquen.rdelay.model.req.create_task.Req4CreateStrContentTask;
import com.fenquen.rdelay.model.task.TaskBase;
import com.fenquen.rdelay.model.req.create_task.Req4CreateTask;
import com.fenquen.rdelay.model.req.modify_task.Req4AbortTaskManually;
import com.fenquen.rdelay.model.resp.Resp4CreateTask;
import com.fenquen.rdelay.model.resp.RespBase;
import com.fenquen.rdelay.model.task.ReflectionTask;
import com.fenquen.rdelay.model.task.StrContentTask;
import com.fenquen.rdelay.model.task.TaskType;
import com.fenquen.rdelay.server.config.Config;
import com.fenquen.rdelay.server.redis.RedisOperator;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.UUID;

@RestController
public class ServerPortal {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerPortal.class);

    @Value("${rdelay.dashboard.topic.name}")
    private String destTopicName;

    @Value("${rdelay.dashboard.enabled}")
    private Boolean dashBoardEnabled;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private RedisOperator redisOperator;

    @RequestMapping(value = "/createTask/STR_CONTENT", method = RequestMethod.POST)
    public RespBase createStrContentTask(@RequestBody Req4CreateStrContentTask req4Create) {
        return processTaskCreation(req4Create);
    }

    @RequestMapping(value = "/createTask/REFLECT", method = RequestMethod.POST)
    public RespBase createReflectTask(@RequestBody Req4CreateReflectionTask req4Create) {
        return processTaskCreation(req4Create);
    }

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
        String[] arr = luaResult.split("&");

        String jsonStr = arr[0];
        Long versionNum = Long.valueOf(arr[1]);
        if (!dashBoardEnabled) {
            return;
        }

        //String jsonStr = redisOperator.getTaskJsonStr(taskId);

        // taskid pattern taskType@uuid
        String taskTypeStr = taskid.split("@")[0];
        TaskType taskType = TaskType.valueOf(taskTypeStr);

        // modify taskState
        TaskBase taskBase = JSON.parseObject(jsonStr, taskType.clazz);
        taskBase.taskState = taskState;
        taskBase.versionNum = versionNum;

        kafkaTemplate.send(destTopicName, taskBase.getDbMetaData().name(), JSON.toJSONString(taskBase));
    }


    private RespBase processTaskCreation(Req4CreateTask req4CreateTask) {
        Resp4CreateTask resp4CreateTask = new Resp4CreateTask();
        try {
            TaskBase task = parseReq4Create(req4CreateTask);

            // redis
            task.versionNum = redisOperator.createTask(task);

            if (null == task.versionNum || Config.FAILURE_VERSION_NUM == task.versionNum) {
                throw new RuntimeException("processTaskCreation,null==task.versionNum");
            }

            // send newly built task
            if (dashBoardEnabled) {
                kafkaTemplate.send(destTopicName, task.getDbMetaData().name(), JSON.toJSONString(task));
            }

            resp4CreateTask.id = task.taskid;
            resp4CreateTask.success();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            resp4CreateTask.fail(e);
        }

        return resp4CreateTask;
    }

    @SuppressWarnings("ConstantConditions")
    private TaskBase parseReq4Create(Req4CreateTask req4Create) throws Exception {
        TaskBase taskBase;

        switch (req4Create.getTaskType()) {
            case STR_CONTENT:
                taskBase = new StrContentTask();
                break;
            case REFLECTION:
                taskBase = new ReflectionTask();
                break;
            default:
                throw new UnsupportedOperationException();
        }

        // common part
        // how to build postProcess rich functionally taskid? taskType@uuid@cronExpression
        taskBase.name = req4Create.name;
        taskBase.taskid = req4Create.getTaskType().name() + "@" + UUID.randomUUID().toString();
        taskBase.bizTag = req4Create.bizTag;
        taskBase.enableCron = req4Create.enableCron;
        taskBase.executionTime = req4Create.executionTime;
        taskBase.cronExpression = req4Create.cronExpression;
        taskBase.maxRetryCount = req4Create.maxRetryCount;
        taskBase.taskReceiveUrl = req4Create.getTaskReceiveUrl();
        taskBase.createTime = new Date().getTime();
        taskBase.taskType = req4Create.getTaskType();
        taskBase.taskState = TaskBase.TaskState.NORMAL;

        // verify the cron expression
        if (taskBase.enableCron) {
            CronExpression cronExpression = new CronExpression(taskBase.cronExpression);
            taskBase.executionTime = cronExpression.getNextValidTimeAfter(new Date()).getTime();
            // save the binding mapping,temporary solution
        }


        // custom part
        switch (req4Create.getTaskType()) {
            case STR_CONTENT:
                ((StrContentTask) taskBase).content = ((Req4CreateStrContentTask) req4Create).content;
                break;
            case REFLECTION:
                ReflectionTask reflectionTask = (ReflectionTask) taskBase;
                Req4CreateReflectionTask req4CreateReflectTask = (Req4CreateReflectionTask) req4Create;

                reflectionTask.className = req4CreateReflectTask.className;
                reflectionTask.methodName = req4CreateReflectTask.methodName;
                reflectionTask.paramTypeNames = req4CreateReflectTask.paramTypeNames;
                reflectionTask.params = req4CreateReflectTask.params;
                break;
        }


        return taskBase;
    }
}

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

    @RequestMapping("/testKafka")
    public String testKafka() {
        kafkaTemplate.send(destTopicName, "TASK", "{\n" +
                "    \"bizTag\": \"testBizTag\",\n" +
                "    \"content\": \"testContent\",\n" +
                "    \"createTime\": 1568011210377,\n" +
                "    \"executionTime\": 1568011211833,\n" +
                "    \"taskid\": \"STR_CONTENT@" + UUID.randomUUID() + "\n" +
                "    \"maxRetryCount\": 3,\n" +
                "    \"myClazzName\": \"com.fenquen.rdelay.model.task.StrContentTask\",\n" +
                "    \"retriedCount\": 1,\n" +
                "    \"taskReceiveUrl\": \"http://127.0.0.1:8080/rdelay/receiveTask/STR_CONTENT\",\n" +
                "    \"taskType\": \"STR_CONTENT\"\n" +
                "}");
        return "{\"success\":true}";
    }

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

            if (!redisOperator.abortTaskManually(req4AbortTaskManually.taskId)) {
                throw new RuntimeException("maybe the task is already already " +
                        "ABORTED_MANUALLY, COMPLETED_NORMALLY or ABORTED_WITH_TOO_MANY_RETRIES");
            }
            modifyState(req4AbortTaskManually.taskId, TaskBase.TaskState.ABORTED_MANUALLY);
            respBase.success();
        } catch (Exception e) {
            respBase.fail(e);
        }
        return respBase;
    }


    @RequestMapping(value = "pauseTask", method = RequestMethod.POST)
    public RespBase pauseTask(@RequestBody Req4PauseTask req4PauseTask) {
        RespBase respBase = new RespBase();
        try {
            if (!redisOperator.pauseTask(req4PauseTask.taskId)) {
                throw new RuntimeException("maybe the task is already already " +
                        "PAUSED,ABORTED_MANUALLY,COMPLETED_NORMALLY or ABORTED_WITH_TOO_MANY_RETRIES");
            }
            modifyState(req4PauseTask.taskId, TaskBase.TaskState.PAUSED);
            respBase.success();
        } catch (Exception e) {
            respBase.fail(e);
        }
        return respBase;
    }

    @RequestMapping(value = "resumeTask", method = RequestMethod.POST)
    public RespBase resumeTask(@RequestBody Req4ResumeTask req4ResumeTask) {
        RespBase respBase = new RespBase();
        try {
            if (!redisOperator.resumeTask(req4ResumeTask.taskId)) {
                throw new RuntimeException("maybe the task is not paused now");
            }
            modifyState(req4ResumeTask.taskId, TaskBase.TaskState.NORMAL);
            respBase.success();
        } catch (Exception e) {
            respBase.fail(e);
        }
        return respBase;
    }

    private void modifyState(String taskId, TaskBase.TaskState taskState) {
        String jsonStr = redisOperator.getTaskJsonStr(taskId);

        // taskid pattern taskType@uuid
        String taskTypeStr = taskId.split("@")[0];
        TaskType taskType = TaskType.valueOf(taskTypeStr);

        // modify taskState
        TaskBase taskBase = JSON.parseObject(jsonStr, taskType.clazz);
        taskBase.taskState = taskState;

        kafkaTemplate.send(destTopicName, taskTypeStr, JSON.toJSONString(taskBase));
    }


    private RespBase processTaskCreation(Req4CreateTask req4CreateTask) {
        Resp4CreateTask resp4CreateTask = new Resp4CreateTask();
        try {
            TaskBase task = parseReq4Create(req4CreateTask);

            // redis
            redisOperator.createTask(task);

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
        TaskBase abstractTask;

        switch (req4Create.getTaskType()) {
            case STR_CONTENT:
                abstractTask = new StrContentTask();
                break;
            case REFLECT:
                abstractTask = new ReflectionTask();
                break;
            default:
                throw new UnsupportedOperationException();
        }

        // common part
        // how to build a rich functionally taskid? taskType@uuid@cronExpression
        abstractTask.taskid = req4Create.getTaskType().name() + "@" + UUID.randomUUID().toString() + "@";
        abstractTask.bizTag = req4Create.bizTag;
        abstractTask.enableCron = req4Create.enableCron;
        abstractTask.executionTime = req4Create.executionTime;
        abstractTask.cronExpression = req4Create.cronExpression;
        abstractTask.maxRetryCount = req4Create.maxRetryCount;
        abstractTask.taskReceiveUrl = req4Create.getTaskReceiveUrl();
        abstractTask.createTime = new Date().getTime();
        abstractTask.taskType = req4Create.getTaskType();
        abstractTask.taskState = TaskBase.TaskState.NORMAL;

        // verify the cron expression
        if (abstractTask.enableCron) {
            CronExpression cronExpression = new CronExpression(abstractTask.cronExpression);
            abstractTask.executionTime = cronExpression.getNextValidTimeAfter(new Date()).getTime();
            // save the binding mapping,temporary solution
        }


        // custom part
        switch (req4Create.getTaskType()) {
            case STR_CONTENT:
                ((StrContentTask) abstractTask).content = ((Req4CreateStrContentTask) req4Create).content;
                break;
            case REFLECT:
                ReflectionTask reflectionTask = (ReflectionTask) abstractTask;
                Req4CreateReflectionTask req4CreateReflectTask = (Req4CreateReflectionTask) req4Create;

                reflectionTask.className = req4CreateReflectTask.className;
                reflectionTask.methodName = req4CreateReflectTask.methodName;
                reflectionTask.paramTypeNames = req4CreateReflectTask.paramTypeNames;
                reflectionTask.params = req4CreateReflectTask.params;
                break;
        }


        return abstractTask;
    }
}

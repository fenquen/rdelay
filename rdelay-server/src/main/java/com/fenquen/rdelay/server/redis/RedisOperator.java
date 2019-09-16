package com.fenquen.rdelay.server.redis;

import com.alibaba.fastjson.JSON;
import com.fenquen.rdelay.server.config.Config;
import com.fenquen.rdelay.model.task.TaskBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

@Component
public class RedisOperator {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private DefaultRedisScript<Boolean> luaScript4TransferBetweenZsets;
    private DefaultRedisScript<Long> luaScript4DeleteTaskCompletely;
    private DefaultRedisScript<Long> luaScript4CreateTask;
    private DefaultRedisScript<Long> luaScript4UpdateTask;

    private DefaultRedisScript<Boolean> luaScript4RefreshCronTask;

    private DefaultRedisScript<String> luaScript4AbortTaskManually;
    private DefaultRedisScript<String> luaScript4PauseTask;
    private DefaultRedisScript<String> luaScript4ResumeTask;

    private DefaultRedisScript<Boolean> luascript4TransferAllTemp2Normal;


    @PostConstruct
    public void init() {
        // luaScript4TransferBetweenZsets
        luaScript4TransferBetweenZsets = new DefaultRedisScript<>();
        luaScript4TransferBetweenZsets.setResultType(Boolean.class);
        luaScript4TransferBetweenZsets.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/transferBetweenZsets.lua")));

        // luaScript4DeleteTaskCompletely
        luaScript4DeleteTaskCompletely = new DefaultRedisScript<>();
        luaScript4DeleteTaskCompletely.setResultType(Long.class);
        luaScript4DeleteTaskCompletely.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/deleteTaskCompletely.lua")));

        // luaScript4CreateTask
        luaScript4CreateTask = new DefaultRedisScript<>();
        luaScript4CreateTask.setResultType(Long.class);
        luaScript4CreateTask.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/createTask.lua")));

        // luaScript4DeleteTaskCompletely
        luaScript4UpdateTask = new DefaultRedisScript<>();
        luaScript4UpdateTask.setResultType(Long.class);
        luaScript4UpdateTask.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/updateTask.lua")));

        // luaScript4RefreshCronTask
        luaScript4RefreshCronTask = new DefaultRedisScript<>();
        luaScript4RefreshCronTask.setResultType(Boolean.class);
        luaScript4RefreshCronTask.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/refreshCronTask.lua")));

        // luaScript4AbortTaskManually
        luaScript4AbortTaskManually = new DefaultRedisScript<>();
        luaScript4AbortTaskManually.setResultType(String.class);
        luaScript4AbortTaskManually.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/abortTaskManually.lua")));

        // luaScript4PauseTask
        luaScript4PauseTask = new DefaultRedisScript<>();
        luaScript4PauseTask.setResultType(String.class);
        luaScript4PauseTask.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/pauseTask.lua")));

        // luaScript4ResumeTask
        luaScript4ResumeTask = new DefaultRedisScript<>();
        luaScript4ResumeTask.setResultType(String.class);
        luaScript4ResumeTask.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/resumeTask.lua")));

        // luaScript4ResumeTask
        luascript4TransferAllTemp2Normal = new DefaultRedisScript<>();
        luascript4TransferAllTemp2Normal.setResultType(Boolean.class);
        luascript4TransferAllTemp2Normal.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/resumeTask.lua")));

    }

    public Long createTask(TaskBase task) {
        return stringRedisTemplate.execute(luaScript4CreateTask,
                Collections.singletonList(Config.NORMAL_ZSET),
                task.taskid, JSON.toJSONString(task), Config.TASK_EXPIRE_MS + "", task.executionTime + "");
        // use lua script to combine them as an atomic one
        /*stringRedisTemplate.opsForValue().set(task.taskid, JSON.toJSONString(task), Config.TASK_EXPIRE_MS, TimeUnit.MILLISECONDS);
        stringRedisTemplate.opsForZSet().add(Config.NORMAL_ZSET, task.taskid, task.executionTime);*/
    }

    public String getTaskJsonStr(String id) {
        return stringRedisTemplate.opsForValue().get(id);
    }

    // used only when COMPLETED_NORMALLY,ABORTED_WITH_TOO_MANY_RETRIES,taskid is in TEMP_ZSET
    public Long delTaskCompletely(String taskId) {
        return stringRedisTemplate.execute(luaScript4DeleteTaskCompletely,
                Arrays.asList(Config.NORMAL_ZSET, Config.TEMP_ZSET, Config.RETRY_ZSET), taskId);
        // use lua script to combine them as an atomic one
        /* stringRedisTemplate.delete(taskid);
         stringRedisTemplate.opsForZSet().remove(Config.NORMAL_ZSET, taskid);
        stringRedisTemplate.opsForZSet().remove(Config.TEMP_ZSET, taskid);
         stringRedisTemplate.opsForZSet().remove(Config.RETRY_ZSET, taskid);*/
    }

    public void delTaskIdFromZset(String zsetName, String taskId) {
        stringRedisTemplate.opsForZSet().remove(zsetName, taskId);
    }

    // this method's only usage now is to update retried num when execution fails,in asyncHttp callBack
    public Long updateTask(TaskBase task) {
        return stringRedisTemplate.execute(luaScript4UpdateTask,
                Collections.singletonList(task.taskid), JSON.toJSONString(task));
        // use lua script to combine them as an atomic one
       /* long ttlMs = stringRedisTemplate.getExpire(task.taskid, TimeUnit.MILLISECONDS);
        stringRedisTemplate.opsForValue().set(task.taskid, JSON.toJSONString(task), ttlMs, TimeUnit.MILLISECONDS);*/
    }

    public Set<String> getTaskIdsFromZset(String zsetName, long begin, long end) {
        return stringRedisTemplate.opsForZSet().rangeByScore(zsetName, begin, end, 0, Config.ZSET_PROCESS_BATCH_SIZE);
    }

    public Boolean normal2Temp(String taskId, long score) {
        return transferBetweenZsets(Config.NORMAL_ZSET, Config.TEMP_ZSET, taskId, score);
    }

    public void temp2Delay(String taskId, long score) {
        transferBetweenZsets(Config.TEMP_ZSET, Config.NORMAL_ZSET, taskId, score);
    }

    public void temp2Retry(String taskId, long score) {
        transferBetweenZsets(Config.TEMP_ZSET, Config.RETRY_ZSET, taskId, score);
    }

    public void retry2Normal(String taskId, long score) {
        transferBetweenZsets(Config.RETRY_ZSET, Config.NORMAL_ZSET, taskId, score);
    }

    private Boolean transferBetweenZsets(String srcZsetName, String destZsetName, String taskId, long score) {
        return stringRedisTemplate.execute(luaScript4TransferBetweenZsets,
                Arrays.asList(srcZsetName, destZsetName), taskId, score + "");
        // use lua script to combine them as an atomic one
        /* stringRedisTemplate.opsForZSet().add(destBucketName, taskid, score);
           stringRedisTemplate.opsForZSet().remove(srcBuckName, taskid);*/
    }

    public void refreshCronTask(TaskBase task) {
        stringRedisTemplate.execute(luaScript4RefreshCronTask,
                Arrays.asList(Config.NORMAL_ZSET, Config.TEMP_ZSET),
                task.taskid, task.executionTime + "", JSON.toJSONString(task));
    }

    public String abortTaskManually(String taskid) {
        return stringRedisTemplate.execute(luaScript4AbortTaskManually,
                Arrays.asList(Config.NORMAL_ZSET, Config.TEMP_ZSET, Config.RETRY_ZSET, Config.PAUSE_ZSET), taskid);
    }

    public String pauseTask(String taskid) {
        return stringRedisTemplate.execute(luaScript4PauseTask,
                Arrays.asList(Config.NORMAL_ZSET, Config.TEMP_ZSET, Config.RETRY_ZSET, Config.PAUSE_ZSET), taskid);
    }

    public String resumeTask(String taskid) {
        return stringRedisTemplate.execute(luaScript4ResumeTask,
                Arrays.asList(Config.NORMAL_ZSET, Config.PAUSE_ZSET), taskid);
    }

    public Boolean transferAllTemp2Normal() {
        return stringRedisTemplate.execute(luascript4TransferAllTemp2Normal, Arrays.asList(Config.TEMP_ZSET, Config.NORMAL_ZSET));
    }
}

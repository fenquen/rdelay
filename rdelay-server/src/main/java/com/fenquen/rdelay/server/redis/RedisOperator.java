package com.fenquen.rdelay.server.redis;

import com.alibaba.fastjson.JSON;
import com.fenquen.rdelay.server.config.Config;
import com.fenquen.rdelay.model.task.AbstractTask;
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

    private DefaultRedisScript<Boolean> luaScript4TransferBetweenBuckets;
    private DefaultRedisScript<Boolean> luaScript4DeleteTask;
    private DefaultRedisScript<Boolean> luaScript4CreateTask;
    private DefaultRedisScript<Boolean> luaScript4UpdateTask;

    @PostConstruct
    public void init() {
        // luaScript4TransferBetweenBuckets
        luaScript4TransferBetweenBuckets = new DefaultRedisScript<>();
        luaScript4TransferBetweenBuckets.setResultType(Boolean.class);
        luaScript4TransferBetweenBuckets.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/transferBetweenBuckets.lua")));

        // luaScript4DeleteTask
        luaScript4DeleteTask = new DefaultRedisScript<>();
        luaScript4DeleteTask.setResultType(Boolean.class);
        luaScript4DeleteTask.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/deleteTask.lua")));

        // luaScript4CreateTask
        luaScript4CreateTask = new DefaultRedisScript<>();
        luaScript4CreateTask.setResultType(Boolean.class);
        luaScript4CreateTask.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/createTask.lua")));

        // luaScript4DeleteTask
        luaScript4UpdateTask = new DefaultRedisScript<>();
        luaScript4UpdateTask.setResultType(Boolean.class);
        luaScript4UpdateTask.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/updateTask.lua")));
    }

    public void createTask(AbstractTask task) {
        stringRedisTemplate.execute(luaScript4CreateTask, Collections.singletonList(Config.NORMAL_ZSET), task.id, JSON.toJSONString(task), Config.TASK_EXPIRE_MS + "", task.executionTime + "");
        // use lua script to combine them as an atomic one
        /*stringRedisTemplate.opsForValue().set(task.taskId, JSON.toJSONString(task), Config.TASK_EXPIRE_MS, TimeUnit.MILLISECONDS);
        stringRedisTemplate.opsForZSet().add(Config.NORMAL_ZSET, task.taskId, task.executionTime);*/

    }

    public String getTaskJsonStr(String id) {
        return stringRedisTemplate.opsForValue().get(id);
    }


    public void deleteTask(String taskId) {
        stringRedisTemplate.execute(luaScript4DeleteTask, Arrays.asList(Config.NORMAL_ZSET, Config.TEMP_ZSET, Config.RETRY_ZSET), taskId);
        // use lua script to combine them as an atomic one
        /* stringRedisTemplate.delete(taskId);
         stringRedisTemplate.opsForZSet().remove(Config.NORMAL_ZSET, taskId);
        stringRedisTemplate.opsForZSet().remove(Config.TEMP_ZSET, taskId);
         stringRedisTemplate.opsForZSet().remove(Config.RETRY_ZSET, taskId);*/
    }

    public void updateTask(AbstractTask task) {
        stringRedisTemplate.execute(luaScript4UpdateTask, Collections.singletonList(task.id), JSON.toJSONString(task));
        // use lua script to combine them as an atomic one
       /* long ttlMs = stringRedisTemplate.getExpire(task.taskId, TimeUnit.MILLISECONDS);
        stringRedisTemplate.opsForValue().set(task.taskId, JSON.toJSONString(task), ttlMs, TimeUnit.MILLISECONDS);*/
    }

    public Set<String> getTaskIDsFromBucket(String bucketName, long begin, long end) {
        return stringRedisTemplate.opsForZSet().rangeByScore(bucketName, begin, end, 0, Config.BUCKET_PROCESS_BATCH_SIZE);
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

    public Boolean transferBetweenZsets(String srcZsetName, String destZsetName, String taskId, long score) {
        return stringRedisTemplate.execute(luaScript4TransferBetweenBuckets, Arrays.asList(srcZsetName, destZsetName), taskId, score + "");
        // use lua script to combine them as an atomic one
        /* stringRedisTemplate.opsForZSet().add(destBucketName, taskId, score);
           stringRedisTemplate.opsForZSet().remove(srcBuckName, taskId);*/
    }
}

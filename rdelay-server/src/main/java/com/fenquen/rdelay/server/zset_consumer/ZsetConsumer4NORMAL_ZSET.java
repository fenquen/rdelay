package com.fenquen.rdelay.server.zset_consumer;

import com.alibaba.fastjson.JSON;
import com.fenquen.rdelay.model.TaskType;
import com.fenquen.rdelay.model.task.ReflectionTask;
import com.fenquen.rdelay.model.task.StrContentTask;
import com.fenquen.rdelay.server.config.Config;
import com.fenquen.rdelay.model.task.AbstractTask;
import com.fenquen.rdelay.model.execution.ExecutionResp;
import com.fenquen.rdelay.server.redis.RedisOperator;
import com.fenquen.rdelay.utils.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.IntStream;

@EnableScheduling
@Component
public class ZsetConsumer4NORMAL_ZSET extends ZsetConsumerBase implements InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZsetConsumer4NORMAL_ZSET.class);

    private static final List<LinkedBlockingQueue<String>> TASK_ID_QUEUE_LIST = new ArrayList<>(Config.NORMAL_ZSET_CONSUME_QUEUE_COUNT);

    private static final ExecutorService TASK_ID_QUEUE_CONSUMER_POOL = Executors.newFixedThreadPool(Config.NORMAL_ZSET_CONSUME_QUEUE_COUNT, runnable -> {
        Thread thread = new Thread(runnable);
        thread.setName("TASK_ID_QUEUE_PROCESS_" + thread.getId());
        thread.setDaemon(true);
        return thread;
    });

    @Autowired
    private RedisOperator redisOperator;

    @Scheduled(fixedDelay = NORMAL_ZSET_CONSUME_INTERVAL_MS)
    @Override
    public void consume() {
        long now = System.currentTimeMillis();

        // TASK_EXPIRE_MS
        long begin = now - Config.TASK_EXPIRE_MS;

        Set<String> taskIds = redisOperator.getTaskIDsFromBucket(Config.NORMAL_ZSET, begin, now);
        // LOGGER.info("ZsetConsumer4NORMAL_ZSET consume taskId count " + taskIds.size());
        LOGGER.info("ZsetConsumer4NORMAL_ZSET get from NORMAL_ZSET taskIds {}", taskIds);
        for (String taskId : taskIds) {
            dispatchTaskId2Queue(taskId);
        }
    }

    private void dispatchTaskId2Queue(String taskId) {
        //  LOGGER.info("ZsetConsumer4NORMAL_ZSET get from NORMAL_ZSET " + taskId);
        TASK_ID_QUEUE_LIST.get(Math.abs(taskId.hashCode()) % Config.NORMAL_ZSET_CONSUME_QUEUE_COUNT).add(taskId);
    }

    private void processTaskIdFromNormalZset(String taskId) {
        redisOperator.normal2Temp(taskId, System.currentTimeMillis());

        String taskJsonStr = redisOperator.getTaskJsonStr(taskId);

        // not an elegant style to determine which sub class by a json string barely,need to be optimized
        String taskTypeStr = taskId.substring(0, taskId.indexOf("@"));
        TaskType taskType = TaskType.valueOf(taskTypeStr);

        AbstractTask task = null;
        switch (taskType) {
            case REFLECT:
                task = JSON.parseObject(taskJsonStr, ReflectionTask.class);
                break;
            case STR_CONTENT:
                task = JSON.parseObject(taskJsonStr, StrContentTask.class);
                break;
        }

        boolean successPostBack = true;
        try {
            LOGGER.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ZsetConsumer4NORMAL_ZSET postStringContent " + task.taskReceiveUrl);
            String timeUpRespJsonStr = HttpUtils.postStringContent(task.taskReceiveUrl , taskJsonStr);
            LOGGER.info("ZsetConsumer4NORMAL_ZSET postStringContent " + timeUpRespJsonStr);
            ExecutionResp timeUpResp = JSON.parseObject(timeUpRespJsonStr, ExecutionResp.class);
            if (!timeUpResp.success) {
                successPostBack = false;
            }
        } catch (Exception e) {
            successPostBack = false;
            LOGGER.error(e.getMessage(), e);
        }

        if (successPostBack) {
            redisOperator.deleteTask(taskId);
            return;
        }

        task.retriedCount++;

        if (task.retriedCount > task.maxRetryCount) {
            redisOperator.deleteTask(taskId);
        }

        redisOperator.updateTask(task);

        int power = 1;
        for (int a = 0; task.retriedCount - 1 > a; a++) {
            power *= 2;
        }

        long score = System.currentTimeMillis() + Config.RETRY_INTERVAL_SECOND * 1000 * power;
        redisOperator.temp2Retry(taskId, score);
    }

    @Override
    public void afterPropertiesSet() {

        IntStream.range(0, Config.NORMAL_ZSET_CONSUME_QUEUE_COUNT).forEach(a -> {
            TASK_ID_QUEUE_LIST.add(new LinkedBlockingQueue<>());
            TASK_ID_QUEUE_CONSUMER_POOL.submit(() -> {
                LinkedBlockingQueue<String> targetQueue = TASK_ID_QUEUE_LIST.get(a);
                for (; ; ) {
                    try {
                        String taskId = targetQueue.take();
                        LOGGER.info("ZsetConsumer4NORMAL_ZSET get from queue " + taskId);
                        processTaskIdFromNormalZset(taskId);
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }

                }
            });
        });
    }
}


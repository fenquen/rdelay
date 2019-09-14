package com.fenquen.rdelay.server.zset_consumer;

import com.alibaba.fastjson.JSON;
import com.fenquen.rdelay.model.task.TaskType;
import com.fenquen.rdelay.model.task.ReflectionTask;
import com.fenquen.rdelay.model.task.StrContentTask;
import com.fenquen.rdelay.server.config.Config;
import com.fenquen.rdelay.model.task.TaskBase;
import com.fenquen.rdelay.server.http.FutureCallBack0;
import com.fenquen.rdelay.server.redis.RedisOperator;
import com.fenquen.rdelay.utils.HttpUtils;
import com.fenquen.rdelay.utils.ThreadSafeWeakMap;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.IntStream;

@EnableScheduling
@Component
public class ZsetConsumer4NORMAL_ZSET extends ZsetConsumerBase implements InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZsetConsumer4NORMAL_ZSET.class);

    private static final List<LinkedBlockingQueue<String>> TASK_ID_QUEUE_LIST =
            new ArrayList<>(Config.NORMAL_ZSET_CONSUME_QUEUE_COUNT);

    private static final ExecutorService TASK_ID_QUEUE_CONSUMER_POOL =
            Executors.newFixedThreadPool(Config.NORMAL_ZSET_CONSUME_QUEUE_COUNT, runnable -> {
                Thread thread = new Thread(runnable);
                thread.setName("TASK_ID_QUEUE_PROCESS_" + thread.getId());
                thread.setDaemon(true);
                return thread;
            });

    @Override
    public void afterPropertiesSet() {
        IntStream.range(0, Config.NORMAL_ZSET_CONSUME_QUEUE_COUNT).forEach(a -> {
            TASK_ID_QUEUE_LIST.add(new LinkedBlockingQueue<>());
            TASK_ID_QUEUE_CONSUMER_POOL.submit(() -> {
                LinkedBlockingQueue<String> targetQueue = TASK_ID_QUEUE_LIST.get(a);
                for (; ; ) {
                    try {
                        String taskId = targetQueue.take();
                        processTaskIdFromNormalZset(taskId);
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            });
        });
    }

    @Autowired
    private RedisOperator redisOperator;

    @Scheduled(fixedDelay = NORMAL_ZSET_CONSUME_INTERVAL_MS)
    @Override
    public void consume() {
        long now = System.currentTimeMillis();

        // TASK_EXPIRE_MS
        long begin = now - Config.TASK_EXPIRE_MS;

        Set<String> taskIds = redisOperator.getTaskIdsFromZset(Config.NORMAL_ZSET, 0, now);
        // LOGGER.info("ZsetConsumer4NORMAL_ZSET consume taskid count " + taskIds.size());
        if (taskIds.size() > 0) {
            LOGGER.info("ZsetConsumer4NORMAL_ZSET get from NORMAL_ZSET taskIds {}", taskIds);
        }
        for (String taskId : taskIds) {
            dispatchTaskId2Queue(taskId);
        }
    }

    private void dispatchTaskId2Queue(String taskId) {
        // the lua transfer script can tackle "if the same taskid is still in the corresponding queue"
        TASK_ID_QUEUE_LIST.get(Math.abs(taskId.hashCode()) % Config.NORMAL_ZSET_CONSUME_QUEUE_COUNT).add(taskId);
    }

    private void processTaskIdFromNormalZset(final String taskId) {
        // this means that taskid is not in NORMAL_ZSET,no need to go ahead.
        if (!redisOperator.normal2Temp(taskId, System.currentTimeMillis())) {
            return;
        }

        // always get from local weakMap first
        String taskJsonStr = TASK_ID_JSONSTR.get(taskId);
        if (null == taskJsonStr) {
            taskJsonStr = redisOperator.getTaskJsonStr(taskId);
            TASK_ID_JSONSTR.put(taskId, taskJsonStr);
        }


        // not an elegant style to determine which sub class of TaskBase it actually is by a json string barely,need to be optimized
        String taskTypeStr = taskId.substring(0, taskId.indexOf("@"));
        TaskType taskType = TaskType.valueOf(taskTypeStr);

        TaskBase task = null;
        switch (taskType) {
            case REFLECT:
                task = JSON.parseObject(taskJsonStr, ReflectionTask.class);
                break;
            case STR_CONTENT:
                task = JSON.parseObject(taskJsonStr, StrContentTask.class);
                break;
        }

        HttpUtils.postStringContentAsync(task.taskReceiveUrl, taskJsonStr, new FutureCallBack0(task));

       /* boolean successPostBack = true;
        try {
            String executionRespJsonStr = HttpUtils.postStringContentSync(task.taskReceiveUrl, taskJsonStr);
            ExecutionResp executionResp = JSON.parseObject(executionRespJsonStr, ExecutionResp.class);
            if (!executionResp.success) {
                successPostBack = false;
            }
        } catch (Exception e) {
            successPostBack = false;
            LOGGER.error(e.getMessage(), e);
        }

        if (successPostBack) {
            // need to know whether the task is cron or not
            if (task.enableCron) {
                CronExpression cronExpression = TASK_ID_CRON_EXPRESSION.get(taskid);

                if (null == cronExpression) {
                    // there is no possibility to throw exception because the expression is verified at Portal first
                    try {
                        cronExpression = new CronExpression(task.cronExpression);
                    } catch (ParseException e) {
                        // impossible
                        LOGGER.error(e.getMessage(), e);
                    }
                    TASK_ID_CRON_EXPRESSION.put(taskid, cronExpression);
                }

                // calc the next execution time
                Date next = cronExpression.getNextValidTimeAfter(new Date());
                task.executionTime = next.getTime();

                // remove taskid from TEMP_ZSET,update task,add taskid to NORMAL_ZSET with new executionTime
                try {
                   // LOGGER.info("cron refresh " + taskid + "_" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(next) + "_" + next.getTime() + "\n");
                    redisOperator.refreshCronTask(task);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }

            } else {
                // not a cron task,it is throw away
                redisOperator.delTaskCompletely(taskid);
            }
            return;
        }

        // execution failed
        task.retriedCount++;
        if (task.retriedCount > task.maxRetryCount) {
            redisOperator.delTaskCompletely(taskid);
        }

        // update retried num
        redisOperator.updateTask(task);

        int power = 1;
        for (int a = 0; task.retriedCount - 1 > a; a++) {
            power *= 2;
        }

        long score = System.currentTimeMillis() + Config.RETRY_INTERVAL_SECOND * 1000 * power;
        redisOperator.temp2Retry(taskid, score);*/
    }

    public static final ThreadSafeWeakMap<String, CronExpression> TASK_ID_CRON_EXPRESSION = new ThreadSafeWeakMap<>(100);

    private static final ThreadSafeWeakMap<String, String> TASK_ID_JSONSTR = new ThreadSafeWeakMap<>(100);
}


package com.fenquen.rdelay.server.http;

import com.alibaba.fastjson.JSON;
import com.fenquen.rdelay.model.Persistence;
import com.fenquen.rdelay.model.resp.ExecutionResp;
import com.fenquen.rdelay.model.task.TaskBase;
import com.fenquen.rdelay.server.utils.SpringUtils;
import com.fenquen.rdelay.server.config.Config;
import com.fenquen.rdelay.server.redis.RedisOperator;
import com.fenquen.rdelay.server.zset_consumer.ZsetConsumer4NORMAL_ZSET;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.util.EntityUtils;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.kafka.core.KafkaTemplate;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

public class FutureCallBack0 implements FutureCallback<HttpResponse> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FutureCallBack0.class);

    private RedisOperator redisOperator = SpringUtils.getBean(RedisOperator.class);
    private TaskBase task;

    private static String destTopicName = "rdealy-dashboard";

    private static Boolean dashBoardEnabled = false;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    static {
        PropertiesFactoryBean propertiesFactoryBean = SpringUtils.getBean(PropertiesFactoryBean.class);
        try {
            destTopicName = propertiesFactoryBean.getObject().getProperty("dispatcher.bridge.taskid");
            dashBoardEnabled = Boolean.valueOf(propertiesFactoryBean.getObject().getProperty("dispatcher.bridge.region"));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public FutureCallBack0(TaskBase abstractTask) {
        this.task = abstractTask;
    }

    @Override
    public void completed(HttpResponse httpResponse) {
        try {
            String executionRespJsonStr = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
            ExecutionResp executionResp = JSON.parseObject(executionRespJsonStr, ExecutionResp.class);
            // sync execution_resp to dashboard
            sendKafka(executionResp.getDbMetaData(), executionRespJsonStr);
            if (executionResp.success) {
                successProcess();
            } else {
                failureProcess();
            }
        } catch (Exception e) {
            failureProcess();
        }
    }

    @Override
    public void failed(Exception e) {
        // need to build a execution resp manually
        ExecutionResp executionResp = new ExecutionResp();
        executionResp.taskId = task.taskid;
        executionResp.fail(e);

        // sync execution_resp to dashboard
        sendKafka(executionResp.getDbMetaData(), JSON.toJSONString(executionResp));

        failureProcess();
    }

    @Override
    public void cancelled() {

    }

    private void sendKafka(Persistence.DbMetaData dbMetaData, String jsonStr) {
        if (dashBoardEnabled) {
            kafkaTemplate.send(destTopicName, dbMetaData.name(), jsonStr);
        }
    }

    private void successProcess() {
        // need to know whether the task is cron or not
        if (task.enableCron) {
            CronExpression cronExpression = ZsetConsumer4NORMAL_ZSET.TASK_ID_CRON_EXPRESSION.get(task.taskid);

            if (null == cronExpression) {
                // there is no possibility to throw exception because the expression is verified at Portal first
                try {
                    cronExpression = new CronExpression(task.cronExpression);
                } catch (ParseException e) {
                    // impossible
                    LOGGER.error(e.getMessage(), e);
                }
                ZsetConsumer4NORMAL_ZSET.TASK_ID_CRON_EXPRESSION.put(task.taskid, cronExpression);
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
            task.versionNum = redisOperator.delTaskCompletely(task.taskid);

            // update and sync taskState to dashboard
            task.taskState = TaskBase.TaskState.COMPLETED_NORMALLY;
            sendKafka(task.getDbMetaData(), JSON.toJSONString(task));

        }
    }

    private void failureProcess() {
        // execution failed
        task.retriedCount++;
        if (task.retriedCount > task.maxRetryCount) {
            task.versionNum = redisOperator.delTaskCompletely(task.taskid);

            // update and sync taskState to dashboard
            task.taskState = TaskBase.TaskState.ABORTED_WITH_TOO_MANY_RETRIES;
            sendKafka(task.getDbMetaData(), JSON.toJSONString(task));
            return;
        }

        // update retried num
        task.versionNum = redisOperator.updateTask(task);
        // update and sync retried count to dashboard
        sendKafka(task.getDbMetaData(), JSON.toJSONString(task));


        int power = 1;
        for (int a = 0; task.retriedCount - 1 > a; a++) {
            power *= 2;
        }

        long score = System.currentTimeMillis() + Config.RETRY_INTERVAL_SECOND * 1000 * power;
        redisOperator.temp2Retry(task.taskid, score);
    }
}
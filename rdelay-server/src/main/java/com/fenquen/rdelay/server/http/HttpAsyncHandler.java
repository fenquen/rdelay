package com.fenquen.rdelay.server.http;

import com.alibaba.fastjson.JSON;
import com.fenquen.rdelay.exception.TaskReceiveFailException;
import com.fenquen.rdelay.model.Persistence;
import com.fenquen.rdelay.model.common.Pair;
import com.fenquen.rdelay.model.resp.ExecutionResp;
import com.fenquen.rdelay.model.resp.ReceiveResp;
import com.fenquen.rdelay.model.task.TaskBase;
import com.fenquen.rdelay.server.config.Config;
import com.fenquen.rdelay.server.redis.RedisOperator;
import com.fenquen.rdelay.server.zset_consumer.ZsetConsumer4NORMAL_ZSET;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.util.EntityUtils;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class HttpAsyncHandler implements FutureCallback<HttpResponse>, InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpAsyncHandler.class);

    @Autowired
    private RedisOperator redisOperator;// = SpringUtils.getBean(RedisOperator.class);
    private static RedisOperator redisOperator_;

    @Value("${rdelay.dashboard.topic.name}")
    private String destTopicName;//= "rdealy-dashboard";
    private static String destTopicName_;

    @Value("${rdelay.dashboard.enabled}")
    private Boolean dashBoardEnabled;//= false;
    private static Boolean dashBoardEnabled_;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    private static KafkaTemplate<String, String> kafkaTemplate_;

    private TaskBase task;

    public static final ConcurrentHashMap<String, Pair<ReceiveResp, TaskBase>> TASK_ID_PAIR = new ConcurrentHashMap<>();

    public HttpAsyncHandler() {
    }

    public HttpAsyncHandler(TaskBase taskBase) {
        //  SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        this.task = taskBase;
    }

    @Override
    public void completed(HttpResponse httpResponse) {
        try {
            String executionRespJsonStr = EntityUtils.toString(httpResponse.getEntity(), "utf-8");

            ReceiveResp receiveResp = JSON.parseObject(executionRespJsonStr, ReceiveResp.class);

            // task execution success includes 2 steps:1st task receive successes,2nd task execution itself successes
            if (receiveResp.success) {
                // wait for the task execution itself to determine next step
                // store in memory storage
                TASK_ID_PAIR.put(receiveResp.taskid, new Pair<>(receiveResp, task));

            } else {
                // task receive fail means task execution fails,manually build execution resp
                ExecutionResp executionResp_ = new ExecutionResp(task,new TaskReceiveFailException(receiveResp.errMsg));

                // sync execution_resp to dashboard
                sendKafka(executionResp_);
                failureProcess(task);
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            failureProcess(task);
        }
    }

    @Override
    public void failed(Exception e) {
        LOGGER.error(e.getMessage(), e);
        // need to build a execution resp manually
        ExecutionResp executionResp = new ExecutionResp(task,e);

        // sync execution_resp to dashboard
        sendKafka(executionResp);

        failureProcess(task);
    }

    @Override
    public void cancelled() {

    }

    public void sendKafka(Persistence persistence){
        if (dashBoardEnabled_) {
            kafkaTemplate_.send(destTopicName_, persistence.getDbMetaData().name(), JSON.toJSONString(persistence));
        }
    }

    public void successProcess(TaskBase task) {
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
                redisOperator_.refreshCronTask(task);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }

        } else {
            // not a cron task,it is throw away
            task.versionNum = redisOperator_.delTaskCompletely(task.taskid);

            // update and sync taskState to dashboard
            task.taskState = TaskBase.TaskState.COMPLETED_NORMALLY;
            sendKafka(task);

        }
    }

    public void failureProcess(TaskBase task) {
        // execution failed
        task.retriedCount++;
        if (task.retriedCount > task.maxRetryCount) {
            task.versionNum = redisOperator_.delTaskCompletely(task.taskid);

            // update and sync taskState to dashboard
            task.taskState = TaskBase.TaskState.ABORTED_WITH_TOO_MANY_RETRIES;
            sendKafka(task);
            return;
        }

        // update retried num
        task.versionNum = redisOperator_.updateTask(task);
        // update and sync retried count to dashboard
        sendKafka(task);


        int power = 1;
        for (int a = 0; task.retriedCount - 1 > a; a++) {
            power *= 2;
        }

        long score = System.currentTimeMillis() + Config.RETRY_INTERVAL_SECOND * 1000 * power;
        redisOperator_.temp2Retry(task.taskid, score);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        kafkaTemplate_ = kafkaTemplate;
        redisOperator_ = redisOperator;
        destTopicName_ = destTopicName;
        dashBoardEnabled_ = dashBoardEnabled;
    }
}
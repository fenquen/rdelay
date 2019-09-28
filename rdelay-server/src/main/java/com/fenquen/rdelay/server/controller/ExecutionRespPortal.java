package com.fenquen.rdelay.server.controller;

import com.alibaba.fastjson.JSON;
import com.fenquen.rdelay.model.common.Pair;
import com.fenquen.rdelay.model.resp.ExecutionResp;
import com.fenquen.rdelay.model.resp.ReceiveResp;
import com.fenquen.rdelay.model.resp.RespBase;
import com.fenquen.rdelay.model.task.TaskBase;
import com.fenquen.rdelay.server.http.HttpAsyncHandler;
import com.fenquen.rdelay.server.redis.RedisOperator;
import com.fenquen.rdelay.server.utils.RdelayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@RestController
public class ExecutionRespPortal {

    @Autowired
    private HttpAsyncHandler httpAsyncHandler;

    @Autowired
    private RedisOperator redisOperator;

    private ExecutorService EXEC_RESP_PROCESS_POOL = Executors.newFixedThreadPool(4, runnable -> {
        Thread thread = new Thread(runnable);
        thread.setName("EXEC_RESP_PROCESS_POOL" + UUID.randomUUID());
        return thread;
    });

    @RequestMapping(value = "/receiveExecResp", method = RequestMethod.POST)
    public RespBase receiveExecResp(@RequestBody ExecutionResp executionResp) {

        EXEC_RESP_PROCESS_POOL.submit(() -> {
            httpAsyncHandler.sendKafka(executionResp);

            Pair<ReceiveResp, TaskBase> pair = HttpAsyncHandler.TASK_ID_PAIR.remove(executionResp.taskid);

            // means receiver actually has received task but failed to let server know
            if (pair == null) {
                String taskJsonStr = redisOperator.getTaskJsonStr(executionResp.taskid);
                TaskBase taskBase = RdelayUtils.parseTask(executionResp.taskid, taskJsonStr);
                pair = new Pair<>(null, taskBase);

            }

            executionResp.receiveResp = pair.left;

            if (executionResp.success) {
                httpAsyncHandler.successProcess(pair.right);
            } else {
                httpAsyncHandler.failureProcess(pair.right);
            }
        });

        return new RespBase().success();
    }
}

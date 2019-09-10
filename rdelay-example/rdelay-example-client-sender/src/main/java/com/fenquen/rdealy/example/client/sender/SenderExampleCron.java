package com.fenquen.rdealy.example.client.sender;

import com.fenquen.rdealy.client.sender.TaskSender;
import com.fenquen.rdelay.model.req.create_task.Req4CreateStrContentTask;
import com.fenquen.rdelay.model.resp.Resp4CreateTask;

public class SenderExampleCron {
    public static void main(String[] args) throws Exception {
        // designate the rdelay server address where the task to be sent
        TaskSender.setDestSvrAddr("http://127.0.0.1:8086");


        Req4CreateStrContentTask req4CreateStrContentTask = new Req4CreateStrContentTask();

        req4CreateStrContentTask.bizTag = "testBizTag";

        // enable cron
        req4CreateStrContentTask.enableCron = true;
        // when cron is enabled,"executionTime" will be ignored
        req4CreateStrContentTask.executionTime = System.currentTimeMillis() + 2000;
        // cron expression,executed per 2h
        req4CreateStrContentTask.cronExpression = "0/10 * * * * ? ";

        req4CreateStrContentTask.executionAppSvrAddr = "http://127.0.0.1:8080";
        req4CreateStrContentTask.content = "testContent";

        Resp4CreateTask resp4CreateTask = TaskSender.sendTask(req4CreateStrContentTask);

        System.out.println(resp4CreateTask.success + "_" + resp4CreateTask.errMsg);

    }
}
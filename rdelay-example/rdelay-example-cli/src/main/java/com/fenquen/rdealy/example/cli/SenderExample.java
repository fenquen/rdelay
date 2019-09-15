package com.fenquen.rdealy.example.cli;

import com.fenquen.rdealy.client.sender.RdelayCli;
import com.fenquen.rdelay.model.req.create_task.Req4CreateReflectionTask;
import com.fenquen.rdelay.model.req.create_task.Req4CreateStrContentTask;
import com.fenquen.rdelay.model.resp.Resp4CreateTask;

public class SenderExample {
    public static void main(String[] args) throws Exception {
        sendCronTask();
    }

    public static void sendTask() throws Exception {
        // designate the rdelay server address where the task to be sent
        RdelayCli.setDestSvrAddr("http://127.0.0.1:8086");

        //  while (true) {
        Req4CreateStrContentTask req4CreateStrContentTask = new Req4CreateStrContentTask();

        req4CreateStrContentTask.bizTag = "testBizTag";
        // run after 2s
        req4CreateStrContentTask.executionTime = System.currentTimeMillis() + 2000;
        req4CreateStrContentTask.executionAppSvrAddr = "http://127.0.0.1:8080";
        req4CreateStrContentTask.content = "testContent";

        Resp4CreateTask resp4CreateTask = RdelayCli.sendTask(req4CreateStrContentTask);

        System.out.println(resp4CreateTask.success + "_" + resp4CreateTask.errMsg);

        //  Thread.sleep(60000000);
        //  }
    }

    public static void sendCronTask() throws Exception {
        // designate the rdelay server address where the task to be sent
        RdelayCli.setDestSvrAddr("http://127.0.0.1:8086");


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

        Resp4CreateTask resp4CreateTask = RdelayCli.sendTask(req4CreateStrContentTask);

        System.out.println(resp4CreateTask.success + "_" + resp4CreateTask.errMsg);

    }

    public static void sendReflectionTask() throws Exception {
        // designate the rdelay server address where the task to be sent
        RdelayCli.setDestSvrAddr("http://127.0.0.1:8086");

        Req4CreateReflectionTask req4CreateReflectionTask = new Req4CreateReflectionTask();

        req4CreateReflectionTask.bizTag = "testBizTag";

        // enable cron
        req4CreateReflectionTask.enableCron = true;
        // when cron is enabled,"executionTime" will be ignored
        req4CreateReflectionTask.executionTime = System.currentTimeMillis() + 2000;
        // cron expression,executed per 2h
        req4CreateReflectionTask.cronExpression = "0/10 * * * * ? ";

        req4CreateReflectionTask.executionAppSvrAddr = "http://127.0.0.1:8080";

        // necessary for reflection
        req4CreateReflectionTask.className = "com.a.d";
        req4CreateReflectionTask.methodName = "helloWorld";
        req4CreateReflectionTask.paramTypeNames = new String[]{"java.lang.String"};
        req4CreateReflectionTask.params = new String[]{"rdealy"};

        Resp4CreateTask resp4CreateTask = RdelayCli.sendTask(req4CreateReflectionTask);

        System.out.println(resp4CreateTask.success + "_" + resp4CreateTask.errMsg);
    }
}
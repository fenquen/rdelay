package com.fenquen.rdealy.example.client.receiver;

import com.fenquen.rdealy.client.send.TaskSender;
import com.fenquen.rdelay.model.req.create_task.Req4CreateStrContentTask;
import com.fenquen.rdelay.model.resp.Resp4CreateTask;

public class SenderExample {
    public static void main(String[] args) throws Exception {
        while (true) {
            Req4CreateStrContentTask req4CreateStrContentTask = new Req4CreateStrContentTask();

            req4CreateStrContentTask.bizTag = "testBizTag";
            // run after 10s
            req4CreateStrContentTask.executionTime = System.currentTimeMillis() + 2000;
            req4CreateStrContentTask.executionAddr = "http://127.0.0.1:8080";
            req4CreateStrContentTask.content = "testContent";

            Resp4CreateTask resp4CreateTask = TaskSender.sendTask(req4CreateStrContentTask);

            System.out.println(resp4CreateTask.success + "_" + resp4CreateTask.errMsg);

            Thread.sleep(60000000);
        }

    }
}

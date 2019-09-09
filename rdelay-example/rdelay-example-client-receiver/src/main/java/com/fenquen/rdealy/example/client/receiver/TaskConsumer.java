package com.fenquen.rdealy.example.client.receiver;

import com.fenquen.rdelay.client.receive.StrContentTaskConsumer;
import com.fenquen.rdelay.model.task.StrContentTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaskConsumer implements StrContentTaskConsumer {
    {
        System.out.println("aaaaaaaaaaaaa");
    }

    @Override
    public void consumeTask(StrContentTask strContentTask) throws Exception {
        System.out.println(strContentTask.content);
    }
}

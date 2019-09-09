package com.fenquen.rdealy.example.client.receiver;

import com.fenquen.rdelay.client.receiver.StrContentTaskConsumer;
import com.fenquen.rdelay.model.task.StrContentTask;
import org.springframework.stereotype.Component;

@Component
public class TaskConsumer implements StrContentTaskConsumer {
    @Override
    public void consumeTask(StrContentTask strContentTask) {
        System.out.println(strContentTask.content);
    }
}

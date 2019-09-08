package com.fenquen.rdelay.client.receive;

import com.fenquen.rdelay.model.task.AbstractTask;
import com.fenquen.rdelay.model.execution.ExecutionResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Receiver {

    @Autowired
    private TaskConsumer taskConsumer;

    @RequestMapping(value = "/rdelay/receiveTask/", method = RequestMethod.POST)
    public ExecutionResp receive(@RequestBody AbstractTask task) {
        ExecutionResp timeUpResp = new ExecutionResp();
        try {
            taskConsumer.consumeTask(task);
            timeUpResp.success();
        } catch (Exception e) {
            e.printStackTrace();
            timeUpResp.fail(e);
        }

        return timeUpResp;
    }

}

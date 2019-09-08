package com.fenquen.rdelay.client.receive;

import com.fenquen.rdelay.model.task.AbstractTask;
import com.fenquen.rdelay.model.execution.ExecutionResp;
import com.fenquen.rdelay.model.task.ReflectionTask;
import com.fenquen.rdelay.model.task.StrContentTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Receiver {

    @Autowired(required = false)
    private TaskConsumer taskConsumer;

    @RequestMapping(value = "/rdelay/receiveTask/STR_CONTENT", method = RequestMethod.POST)
    public ExecutionResp receiveStrContentTask(@RequestBody StrContentTask task) {
        ExecutionResp timeUpResp = new ExecutionResp();
        try {
            if (taskConsumer != null) {
                taskConsumer.consumeTask(task);
            }
            timeUpResp.success();
        } catch (Exception e) {
            e.printStackTrace();
            timeUpResp.fail(e);
        }

        return timeUpResp;
    }

    @RequestMapping(value = "/rdelay/receiveTask/REFLECT", method = RequestMethod.POST)
    public ExecutionResp receiveReflectTask(@RequestBody ReflectionTask task) {
        ExecutionResp timeUpResp = new ExecutionResp();
        try {
            timeUpResp.success();
        } catch (Exception e) {
            e.printStackTrace();
            timeUpResp.fail(e);
        }

        return timeUpResp;
    }

}

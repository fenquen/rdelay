package com.fenquen.rdelay.client.receive;

import com.fenquen.rdelay.model.Task;
import com.fenquen.rdelay.model.timeup.TimeUpReq;
import com.fenquen.rdelay.model.timeup.TimeUpResp;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Receiver {

    @Autowired
    private TaskConsumer taskConsumer;

    @RequestMapping(value = "/rdelay/receiveTask", method = RequestMethod.POST)
    public TimeUpResp receive(@RequestBody Task task) {
        TimeUpResp timeUpResp = new TimeUpResp();
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

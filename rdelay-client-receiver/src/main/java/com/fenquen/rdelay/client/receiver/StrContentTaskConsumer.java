package com.fenquen.rdelay.client.receiver;

import com.fenquen.rdelay.model.task.StrContentTask;

/**
 * used for receiver to process task in its custom way
 * Attention,this interface must only have one impl class
 */
public interface StrContentTaskConsumer {
    void consumeTask(StrContentTask strContentTask) throws Exception;
}

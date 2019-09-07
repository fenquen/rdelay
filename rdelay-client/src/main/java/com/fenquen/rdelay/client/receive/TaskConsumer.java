package com.fenquen.rdelay.client.receive;

import com.fenquen.rdelay.model.Task;

/**
 * used for receiver to process task in its custom way
 * Attention,this interface must only have one impl class
 */
public interface TaskConsumer {
    void consumeTask(Task task) throws Exception;
}

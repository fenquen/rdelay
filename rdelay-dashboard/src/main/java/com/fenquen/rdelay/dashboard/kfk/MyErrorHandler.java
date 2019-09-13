package com.fenquen.rdelay.dashboard.kfk;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.ErrorHandler;

public class MyErrorHandler implements ErrorHandler {
    @Override
    public void handle(Exception thrownException, ConsumerRecord<?, ?> data) {

    }
}

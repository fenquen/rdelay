package com.fenquen.rdelay.server.kfk;

import com.fenquen.rdelay.server.controller.Portal;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.logging.log4j.spi.LoggerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.stereotype.Component;


public class MyProducerListener implements ProducerListener<Object, Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyProducerListener.class);

    @Override
    public void onSuccess(String topic, Integer partition, Object key, Object value, RecordMetadata recordMetadata) {

    }

    @Override
    public void onError(String topic, Integer partition, Object key, Object value, Exception exception) {
        LOGGER.info("key {},value {}", key, value);
    }
}

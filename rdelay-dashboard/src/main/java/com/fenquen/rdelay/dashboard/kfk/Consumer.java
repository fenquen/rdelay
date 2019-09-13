package com.fenquen.rdelay.dashboard.kfk;

import com.alibaba.fastjson.JSON;
import com.fenquen.rdelay.model.ModelBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class Consumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Consumer.class);

    //  @Autowired
    //  private MongoTemplate mongoTemplate;

    @KafkaListener(topics = {"${rdelay.dashboard.topic.name}"}, containerFactory = "kafkaListenerContainerFactory")
    public void receive(@Payload String message,
                        @Header("kafka_receivedMessageKey") String key,
                        Acknowledgment acknowledgment) {
        ModelBase.ModelType modelType = ModelBase.ModelType.valueOf(key);
        switch (modelType) {
            case TASK:
            case EXECUTION_RESP:
            default:
                LOGGER.info(message);
        }
        LOGGER.info(message);
        acknowledgment.acknowledge();
    }
}
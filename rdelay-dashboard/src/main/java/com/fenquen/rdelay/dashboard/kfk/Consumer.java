package com.fenquen.rdelay.dashboard.kfk;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fenquen.rdelay.model.ModelBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

// receive data from kafka then save it to mongodb
@Component
public class Consumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Consumer.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @KafkaListener(topics = {"${rdelay.dashboard.topic.name}"}, containerFactory = "kafkaListenerContainerFactory")
    public void receive(@Payload String message,
                        @Header("kafka_receivedMessageKey") String modelTypeName,
                        Acknowledgment acknowledgment) {
        LOGGER.info(message);

        ModelBase.DbMetaData dbMetaData = null;
        try {
            dbMetaData = ModelBase.DbMetaData.valueOf(modelTypeName);
        } catch (Exception e) {
            LOGGER.info("unrecognized ModelType {}", modelTypeName);
        }

        // discard
        if (null == dbMetaData) {
            acknowledgment.acknowledge();
            return;
        }

        JSONObject jsonObject = JSON.parseObject(message);

        try {
            mongoTemplate.insert(jsonObject, dbMetaData.tableName);
        } catch (Exception e) {
            LOGGER.error("save mongodb ", e);
        }


        switch (dbMetaData) {
            case TASK:
                break;
            case EXECUTION_RESP:
                break;
        }

        acknowledgment.acknowledge();
    }
}
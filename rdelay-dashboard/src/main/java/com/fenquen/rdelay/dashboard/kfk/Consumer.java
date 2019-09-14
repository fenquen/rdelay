package com.fenquen.rdelay.dashboard.kfk;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fenquen.rdelay.model.ModelBase;
import com.fenquen.rdelay.model.task.TaskBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
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
                        @Header("kafka_receivedMessageKey") String dbMetaDataName,
                        Acknowledgment acknowledgment) {
        LOGGER.info(message);

        ModelBase.DbMetaData dbMetaData = null;
        try {
            dbMetaData = ModelBase.DbMetaData.valueOf(dbMetaDataName);
        } catch (Exception e) {
            LOGGER.info("unrecognized ModelType {}", dbMetaDataName);
        }

        // discard
        if (null == dbMetaData) {
            acknowledgment.acknowledge();
            return;
        }

        JSONObject jsonObject = JSON.parseObject(message);


        try {

            if (dbMetaData == ModelBase.DbMetaData.TASK) {
                Query query = new Query(Criteria.where("taskid").is(jsonObject.getString("taskid")));
                // deal with task update(retriedCount++ when execution fails or taskState changes) somehow rigid
                mongoTemplate.findAndRemove(query, TaskBase.class, dbMetaData.tableName);
              //  mongoTemplate.updateMulti(query, new Update().set("retriedCount", jsonObject.getInteger("retriedCount")), dbMetaData.tableName);
              //  return;

            }

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
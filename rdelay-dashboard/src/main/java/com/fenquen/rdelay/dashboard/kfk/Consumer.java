package com.fenquen.rdelay.dashboard.kfk;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fenquen.rdelay.model.Persistence;
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

        Persistence.DbMetaData dbMetaData = null;
        try {
            dbMetaData = Persistence.DbMetaData.valueOf(dbMetaDataName);
        } catch (Exception e) {
            LOGGER.info("unrecognized DbMetaData {}", dbMetaDataName);
        }

        // discard,don't know where to save data
        if (null == dbMetaData) {
            acknowledgment.acknowledge();
            return;
        }

        JSONObject jsonObject = JSON.parseObject(message);

        try {
            boolean needGoAhead = true;
            if (dbMetaData == Persistence.DbMetaData.TASK) {

                String taskid = jsonObject.getString("taskid");
                Long versionNum = jsonObject.getLong("versionNum");
                if (versionNum == null) {
                    acknowledgment.acknowledge();
                    return;
                }

                // need to compare versionNums,the biggest is the winner
                Criteria criteria = Criteria.where("taskid").is(taskid).and("versionNum").lt(versionNum);
                Query query = new Query(criteria);
                if (null != mongoTemplate.findAndReplace(query, jsonObject, dbMetaData.tableName)) {
                    needGoAhead = false;
                }
            }

            if(needGoAhead){
                mongoTemplate.insert(jsonObject, dbMetaData.tableName);
            }
            
        } catch (Exception e) {
            LOGGER.error("save mongodb ", e);
        }

        acknowledgment.acknowledge();
    }
}
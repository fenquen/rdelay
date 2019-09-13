package com.fenquen.rdelay.server.kfk;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.LoggingProducerListener;
import org.springframework.kafka.support.ProducerListener;

@org.springframework.context.annotation.Configuration
public class KafkaProducerConfig {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    public KafkaTemplate<?, ?> kafkaTemplate(
            ProducerFactory<Object, Object> kafkaProducerFactory,
            ProducerListener<Object, Object> kafkaProducerListener) {
        KafkaTemplate<Object, Object> kafkaTemplate = new KafkaTemplate<>(kafkaProducerFactory);

        //  kafkaTemplate.setMessageConverter(this.messageConverter);

        kafkaTemplate.setProducerListener(kafkaProducerListener);
        kafkaTemplate.setDefaultTopic("");
        return kafkaTemplate;
    }

    @Bean
    public ProducerListener<Object, Object> kafkaProducerListener() {
        return new MyProducerListener();
    }
}

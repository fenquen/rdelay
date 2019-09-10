package com.fenquen.rdelay.client.receiver;

import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class Configuration {
    @Bean
    public Receiver produceReceiver() {
        return new Receiver();
    }
}

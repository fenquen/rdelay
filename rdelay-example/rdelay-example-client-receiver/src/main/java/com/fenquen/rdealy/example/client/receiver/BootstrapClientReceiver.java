package com.fenquen.rdealy.example.client.receiver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.fenquen.rdealy.example.client.receiver", "com.fenquen.rdelay.client.receiver"})
public class BootstrapClientReceiver {
    public static void main(String[] args) {
        SpringApplication.run(BootstrapClientReceiver.class, args);

    }
}

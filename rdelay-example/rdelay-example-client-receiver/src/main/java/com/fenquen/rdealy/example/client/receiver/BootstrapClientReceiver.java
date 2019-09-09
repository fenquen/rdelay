package com.fenquen.rdealy.example.client.receiver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// need to add scan the package "com.fenquen.rdelay.client.receiver",the server is listening 127.0.0.1:8080
@SpringBootApplication(scanBasePackages = {"com.fenquen.rdealy.example.client.receiver", "com.fenquen.rdelay.client.receiver"})
public class BootstrapClientReceiver {
    public static void main(String[] args) {
        SpringApplication.run(BootstrapClientReceiver.class, args);

    }
}

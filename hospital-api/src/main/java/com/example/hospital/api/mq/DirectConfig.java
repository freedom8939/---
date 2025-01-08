package com.example.hospital.api.mq;


import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DirectConfig {

    @Bean
    public Queue prescriptionQueue() {
        return new Queue("prescription");
    }
}

package com.test.service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Profile("rabbitmq")
@Configuration
public class RabbitMQConfiguration {

    @Value("${rabbitmq.host}")
    private String host;

    @Bean
    public Connection connection() throws IOException, TimeoutException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        return factory.newConnection();
    }

    @Bean
    public Channel channel(Connection connection) throws IOException {
        return connection.createChannel();
    }
}

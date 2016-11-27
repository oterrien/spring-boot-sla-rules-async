package com.test.service;

import com.rabbitmq.client.*;
import com.test.exchange.Request;
import com.test.exchange.Response;
import com.test.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Service
@Slf4j
@SuppressWarnings("unchecked")
public class RuleCalculatorService {

    @Value("${rabbitmq.queue.rule.name}")
    private String queueName;

    @Autowired
    private RuleService ruleService;

    @Autowired
    private Channel channel;

    @PostConstruct
    public void init() throws Exception {

        log.info("####-RuleCalculatorService started");
        channel.queueDeclare(queueName, false, false, false, null);
        channel.basicQos(1);
        channel.basicConsume(queueName, false, createConsumer());
    }

    private Consumer createConsumer() {

        return new DefaultConsumer(channel) {

            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) throws IOException {

                Request request = JsonUtils.parse(new String(body, "UTF-8"), Request.class);

                log.info(String.format("####-Request received for page #%d - [%s]", request.getPageIndex(), properties.getCorrelationId()));

                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                        .Builder()
                        .correlationId(properties.getCorrelationId())
                        .build();

                Response result = ruleService.applyRules(request);

                String message = JsonUtils.serialize(result);

                channel.basicPublish("", properties.getReplyTo(), replyProps, message.getBytes("UTF-8"));
                channel.basicAck(envelope.getDeliveryTag(), false);

                log.info(String.format("####-Response sent for page #%d - [%s]", request.getPageIndex(), properties.getCorrelationId()));
            }
        };
    }
}

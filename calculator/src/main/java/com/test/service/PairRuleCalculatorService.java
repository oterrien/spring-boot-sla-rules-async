package com.test.service;

import com.rabbitmq.client.*;
import com.test.exchange.Request;
import com.test.exchange.Response;
import com.test.invoice.Invoice;
import com.test.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Profile("pair")
@Service
@Slf4j
public class PairRuleCalculatorService {

    @Value("${rabbitmq.dispatch.queue.name.pair}")
    private String requestQueueName;

    @Autowired
    private RuleService ruleService;

    @Autowired
    private Channel channel;

    @PostConstruct
    public void init() throws Exception {

        log.info("####-PairRuleCalculatorService started");
        channel.basicConsume(requestQueueName, false, createConsumer());
    }

    private Consumer createConsumer() {

        return new DefaultConsumer(channel) {

            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) throws IOException {

                Request<Invoice> request = JsonUtils.parse(new String(body, "UTF-8"), Request.class, Invoice.class);

                log.info(String.format("####-Request received for page #%d - [%s]", request.getPageIndex(), properties.getCorrelationId()));

                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                        .Builder()
                        .correlationId(properties.getCorrelationId())
                        .build();

                Response<Invoice> result = ruleService.applyRules(request);

                String message = JsonUtils.serialize(result);
                channel.basicPublish(StringUtils.EMPTY, properties.getReplyTo(), replyProps, message.getBytes("UTF-8"));
                channel.basicAck(envelope.getDeliveryTag(), false);

                log.debug(String.format("####-Response sent for page #%d - [%s]", request.getPageIndex(), properties.getCorrelationId()));
            }
        };
    }

}

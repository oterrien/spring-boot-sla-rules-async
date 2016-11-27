package com.test.service;

import com.rabbitmq.client.*;
import com.test.exchange.Request;
import com.test.exchange.Response;
import com.test.persistence.repository.IInvoiceRepository;
import com.test.rule.Rule;
import com.test.util.JsonUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Profile("rabbitmq")
@Service
@Slf4j
public class AsyncInvoiceRuleService {

    @Value("${rabbitmq.queue.rule.name}")
    private String queueName;

    @Autowired
    private IInvoiceRepository invoiceRepository;

    @Autowired
    private ResponseHandlerService responseHandlerService;

    @Autowired
    private Channel channel;

    private Map<String, AtomicResponse> responseMap = new ConcurrentHashMap<>();

    @Data
    @RequiredArgsConstructor
    class AtomicResponse {

        private final CountDownLatch countDownLatch;
        private final List<Response> responses = new ArrayList<>();

        void addAndCountdown(Response response) {
            synchronized (this) {
                responses.add(response);
                countDownLatch.countDown();
            }
        }
    }

    public void loadAndApplyRules(List<Rule> rules, final int pageSize) {

        final String correlationId = UUID.randomUUID().toString();

        try {
            int count = (int) invoiceRepository.count();
            final int numberOfPages = (count % pageSize == 0) ? count / pageSize : count / pageSize + 1;

            log.info(String.format("####-Number of pages to request : %d - [%s]", numberOfPages, correlationId));

            String replyQueueName = channel.queueDeclare().getQueue();
            channel.basicConsume(replyQueueName, true, createConsumer());

            responseMap.put(correlationId, new AtomicResponse(new CountDownLatch(numberOfPages)));

            IntStream.range(0, numberOfPages).
                    parallel().
                    mapToObj(i -> new PageRequest(i, pageSize)).
                    map(pageRequest -> invoiceRepository.findAll(pageRequest)).
                    map(page -> new Request(rules, page.getContent(), page.getNumber())).
                    forEach(request -> sendRequest(replyQueueName, correlationId, request));

            responseMap.get(correlationId).
                    getCountDownLatch().await();

            AtomicInteger numberOfAcceptedElements = new AtomicInteger(0);
            AtomicInteger numberOfRejectedElements = new AtomicInteger(0);
            responseMap.get(correlationId).getResponses().
                    parallelStream().
                    peek(response -> numberOfAcceptedElements.getAndAdd(response.getAcceptedElements().size())).
                    peek(response -> numberOfRejectedElements.getAndAdd(response.getRejectedElements().size())).
                    forEach(response -> responseHandlerService.handleResponse(response));

            log.warn(String.format("###-\tnumber of accepted elements : %d", numberOfAcceptedElements.get()));
            log.warn(String.format("###-\tnumber of rejected elements : %d", numberOfRejectedElements.get()));

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            responseMap.remove(correlationId);
        }
    }

    private void sendRequest(String replyQueueName, String correlationId, Request request) {

        try {
            AMQP.BasicProperties props = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(correlationId)
                    .replyTo(replyQueueName)
                    .build();

            String message = JsonUtils.serialize(request);

            channel.basicPublish(StringUtils.EMPTY, queueName, props, message.getBytes("UTF-8"));

            log.info(String.format("####-Request sent for page #%d - [%s]", request.getPageIndex(), correlationId));

        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private Consumer createConsumer() {

        return new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) {

                try {
                    Response response = JsonUtils.parse(new String(body, "UTF-8"), Response.class);

                    log.info(String.format("####-Response received for page #%d - [%s]", response.getPageIndex(), properties.getCorrelationId()));

                    //responseHandlerService.handleResponse(response);

                    responseMap.get(properties.getCorrelationId()).addAndCountdown(response);

                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        };
    }
}

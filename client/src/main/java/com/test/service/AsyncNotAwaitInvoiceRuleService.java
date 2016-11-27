package com.test.service;

import com.rabbitmq.client.*;
import com.test.exchange.Request;
import com.test.exchange.Response;
import com.test.invoice.Invoice;
import com.test.persistence.repository.IInvoiceRepository;
import com.test.persistence.service.RulePersistenceService;
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

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

@Profile("rabbitmq")
@Service
@Slf4j
@SuppressWarnings("unchecked")
public class AsyncNotAwaitInvoiceRuleService {

    @Value("${rabbitmq.queue.rule.name}")
    private String requestQueueName;

    private String replyQueueName;

    @Autowired
    private RulePersistenceService rulePersistenceService;

    @Autowired
    private IInvoiceRepository invoiceRepository;

    @Autowired
    private ResponseHandlerService responseHandlerService;

    @Autowired
    private Channel channel;

    //region Init...
    @PostConstruct
    public void init() throws IOException {

        this.replyQueueName = channel.queueDeclare().getQueue();
        channel.basicConsume(replyQueueName, true, createConsumer());
    }

    private Consumer createConsumer() {

        return new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) {
                try {
                    Response<Invoice> response = JsonUtils.parse(new String(body, "UTF-8"), Response.class, Invoice.class);

                    log.debug(String.format("####-Response received for page #%d - [%s]", response.getPageIndex(), properties.getCorrelationId()));

                    handleResponse(response);

                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        };
    }
    //endregion

    public void loadAndApplyRules(int pageSize) {

        final List<Rule> rules = rulePersistenceService.getAllByPriority();

        final String correlationId = UUID.randomUUID().toString();

        try {
            final int numberOfPages = getNumberOfPages(pageSize);

            log.debug(String.format("####-Number of pages to request : %d - [%s]", numberOfPages, correlationId));

            IntStream.range(0, numberOfPages).
                    parallel().
                    mapToObj(i -> new PageRequest(i, pageSize)).
                    map(pageRequest -> invoiceRepository.findAll(pageRequest)).
                    map(page -> new Request<>(rules, page.getContent(), page.getNumber())).
                    forEach(request -> sendRequest(correlationId, request));

            log.warn(String.format("####-Stop sending request - [%s]", correlationId));

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private int getNumberOfPages(int pageSize) {

        int count = (int) invoiceRepository.count();
        int numberOfPages = count / pageSize;
        if (count % pageSize != 0) {
            numberOfPages++;
        }
        return numberOfPages;
    }

    private void sendRequest(String correlationId, Request<Invoice> request) {

        try {
            AMQP.BasicProperties props = new AMQP.BasicProperties.
                    Builder().
                    correlationId(correlationId).
                    replyTo(replyQueueName).
                    build();

            String message = JsonUtils.serialize(request);
            channel.basicPublish(StringUtils.EMPTY, requestQueueName, props, message.getBytes("UTF-8"));

            log.debug(String.format("####-Request sent for page #%d - [%s]", request.getPageIndex(), correlationId));

        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void handleResponse(Response<Invoice> response) {

        responseHandlerService.handleResponse(response);
    }
}

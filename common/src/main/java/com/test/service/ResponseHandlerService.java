package com.test.service;

import com.test.exchange.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ResponseHandlerService {

    public <T> void handleResponse(Response<T> response) {

        log.warn(String.format("####-HANDLER-number of elements {accepted:%d, rejected:%d)", response.getAcceptedElements().size(), response.getRejectedElements().size()));
    }
}

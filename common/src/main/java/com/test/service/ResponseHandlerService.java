package com.test.service;

import com.test.exchange.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ResponseHandlerService {

    public void handleResponse(Response response) {

        log.warn(String.format("####-\tnumber of accepted elements : %s", response.getAcceptedElements().size()));

        log.warn(String.format("####-\tnumber of rejected elements : %s", response.getRejectedElements().size()));
    }
}

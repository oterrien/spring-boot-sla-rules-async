package com.test.controller;

import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class TestController {

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity loadAndApplyRules(@RequestBody Event event) {

        return new ResponseEntity(event.getBody(), HttpStatus.ACCEPTED);
    }

    @Data
    public static class Event {
        private String type;
        private Object body;
    }
}

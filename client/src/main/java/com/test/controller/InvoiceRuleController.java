package com.test.controller;

import com.test.service.InvoiceRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/invoices")
@Slf4j
public class InvoiceRuleController {

    @Autowired
    private InvoiceRuleService invoiceRuleService;

    @RequestMapping(method = RequestMethod.POST, value = "/loadAndApplyRule")
    @ResponseBody
    public ResponseEntity loadAndApplyRule(@RequestParam(name = "pageSize", defaultValue = "0") int pageSize) {

        invoiceRuleService.loadAndApplyRules(pageSize);
        return new ResponseEntity(HttpStatus.FOUND);
    }
}

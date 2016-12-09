package com.test.controller;

import com.test.service.AsyncAwaitInvoiceRuleService;
import com.test.service.AsyncDispatcherRuleService;
import com.test.service.AsyncNotAwaitInvoiceRuleService;
import com.test.service.InvoiceRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/invoices")
public class InvoiceRuleController {

    @Autowired
    private InvoiceRuleService invoiceRuleService;

    @Autowired
    private AsyncAwaitInvoiceRuleService asyncAwaitInvoiceRuleService;

    @Autowired
    private AsyncDispatcherRuleService asyncDispatcherRuleService;

    @Autowired
    private AsyncNotAwaitInvoiceRuleService asyncInvoiceRuleService;

    @RequestMapping(method = RequestMethod.POST, value = "/loadAndApplyRules")
    @ResponseBody
    public ResponseEntity loadAndApplyRules() {

        invoiceRuleService.loadAndApplyRules();
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/loadAndApplyRulesAsync")
    @ResponseBody
    public ResponseEntity loadAndApplyRulesAsync(@RequestParam(name = "pageSize", defaultValue = "0") int pageSize,
                                                 @RequestParam(name = "withAwait", defaultValue = "false") boolean withAwait) {

        if (pageSize <= 0) {
            return loadAndApplyRules();
        }

        if (withAwait) {
            asyncAwaitInvoiceRuleService.loadAndApplyRules(pageSize);
            return new ResponseEntity(HttpStatus.OK);
        } else {
            asyncInvoiceRuleService.loadAndApplyRules(pageSize);
            return new ResponseEntity(HttpStatus.ACCEPTED);
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/loadAndApplyRulesWithDispatch")
    @ResponseBody
    public ResponseEntity loadAndApplyRulesWithDispatch(@RequestParam(name = "pageSize", defaultValue = "0") int pageSize) {

        if (pageSize <= 0) {
            return loadAndApplyRules();
        }

        asyncDispatcherRuleService.loadAndApplyRules(pageSize);
        return new ResponseEntity(HttpStatus.ACCEPTED);
    }
}

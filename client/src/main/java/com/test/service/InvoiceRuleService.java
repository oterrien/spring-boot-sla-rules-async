package com.test.service;

import com.test.exchange.Request;
import com.test.exchange.Response;
import com.test.invoice.Invoice;
import com.test.persistence.repository.IInvoiceRepository;
import com.test.persistence.repository.IRuleRepository;
import com.test.rule.Rule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class InvoiceRuleService {

    @Autowired
    private IRuleRepository ruleRepository;

    @Autowired
    private IInvoiceRepository invoiceRepository;

    @Autowired
    private RuleService ruleService;

    @Autowired
    private AsyncInvoiceRuleService asyncInvoiceRuleService;

    @Autowired
    private ResponseHandlerService responseHandlerService;

    public void loadAndApplyRules(int pageSize) {

        List<Rule> rules = getAllByPriority();

        if (pageSize <= 0) {
            loadAndApplyRules(rules);
        } else {
            loadAndApplyRules(rules, pageSize);
        }
    }

    public List<Rule> getAllByPriority() {

        Sort sort = new Sort(Sort.Direction.ASC, "priority");
        return ruleRepository.findAll(sort);
    }

    private void loadAndApplyRules(List<Rule> rules) {

        AtomicInteger numberOfAcceptedElements = new AtomicInteger(0);
        AtomicInteger numberOfRejectedElements = new AtomicInteger(0);

        List<Invoice> invoices = invoiceRepository.findAll();
        Request request = new Request(rules, invoices, 0);
        Response response = ruleService.applyRules(request);
        responseHandlerService.handleResponse(response);

        numberOfAcceptedElements.getAndAdd(response.getAcceptedElements().size());
        numberOfRejectedElements.getAndAdd(response.getRejectedElements().size());

        log.warn(String.format("###-\tnumber of accepted elements : %d", numberOfAcceptedElements.get()));
        log.warn(String.format("###-\tnumber of rejected elements : %d", numberOfRejectedElements.get()));
    }

    private void loadAndApplyRules(List<Rule> rules, int pageSize) {

        asyncInvoiceRuleService.loadAndApplyRules(rules, pageSize);
    }
}
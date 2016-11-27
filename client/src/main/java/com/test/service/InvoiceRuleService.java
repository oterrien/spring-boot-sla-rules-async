package com.test.service;

import com.test.exchange.Request;
import com.test.exchange.Response;
import com.test.invoice.Invoice;
import com.test.persistence.repository.IInvoiceRepository;
import com.test.persistence.repository.IRuleRepository;
import com.test.persistence.service.RulePersistenceService;
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
    private RulePersistenceService rulePersistenceService;

    @Autowired
    private IInvoiceRepository invoiceRepository;

    @Autowired
    private RuleService ruleService;

    @Autowired
    private ResponseHandlerService responseHandlerService;

    public void loadAndApplyRules() {

        final List<Rule> rules = rulePersistenceService.getAllByPriority();

        List<Invoice> invoices = invoiceRepository.findAll();
        Request<Invoice> request = new Request<>(rules, invoices, 0);
        Response<Invoice> response = ruleService.applyRules(request);

        handleResponse(response);
    }

    private void handleResponse(Response<Invoice> response) {
        responseHandlerService.handleResponse(response);
    }
}
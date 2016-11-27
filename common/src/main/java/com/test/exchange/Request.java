package com.test.exchange;

import com.test.invoice.Invoice;
import com.test.rule.Rule;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class Request {

    private final List<Rule> rules;

    private final List<Invoice> entities;

    private final int pageIndex;
}

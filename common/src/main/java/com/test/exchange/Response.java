package com.test.exchange;

import com.test.invoice.Invoice;
import com.test.rule.Element;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Response{

    private final List<Invoice> acceptedElements = new ArrayList<>();

    private final List<Element<Invoice>> rejectedElements = new ArrayList<>();

    private int pageIndex;
}

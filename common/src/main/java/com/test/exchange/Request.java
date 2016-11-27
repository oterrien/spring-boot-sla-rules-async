package com.test.exchange;

import com.test.rule.Rule;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class Request<T> {

    private final List<Rule> rules;

    private final List<T> entities;

    private final int pageIndex;
}

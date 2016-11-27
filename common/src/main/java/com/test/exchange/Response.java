package com.test.exchange;

import com.test.rule.Element;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
public class Response<T> {

    private final List<T> acceptedElements = Collections.synchronizedList(new ArrayList<>());

    private final List<Element<T>> rejectedElements = Collections.synchronizedList(new ArrayList<>());

    private int pageIndex;
}

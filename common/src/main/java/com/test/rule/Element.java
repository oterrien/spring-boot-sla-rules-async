package com.test.rule;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Vector;

@Data
@RequiredArgsConstructor
public class Element<T> {

    private final T bean;

    // Use a vector to allow parallelStream to update this field
    private final List<String> rejectionCodes = new Vector<>();

    private Status status = Status.ACCEPTED;

    public enum Status {
        ACCEPTED, REJECTED;
    }
}

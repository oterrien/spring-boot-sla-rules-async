package com.test.rule;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
@NoArgsConstructor
public class GenericRule implements IRule {

    @Getter
    private Integer priority;

    @Getter
    private String name;

    @Getter
    private String description;

    @Getter
    private String rejectionCode;

    @Getter
    @JsonIgnore
    private Predicate keyPredicate, valuePredicate;

    @Override
    public IRule withPriority(Integer priority) {
        this.priority = priority;
        return this;
    }

    @Override
    public IRule withName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public IRule withDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public IRule withRejectionCode(String rejectionCode) {
        this.rejectionCode = rejectionCode;
        return this;
    }

    @Override
    public IRule withParameter(List<Parameter> parameters) {
        Map<Boolean, List<Parameter>> mapGroupedByKeyBooleanValue = parameters.
                stream().
                collect(Collectors.groupingBy(Parameter::isKey));
        this.keyPredicate = extractPredicate(mapGroupedByKeyBooleanValue.get(true), Predicate::and);
        this.valuePredicate = extractPredicate(mapGroupedByKeyBooleanValue.get(false), Predicate::or);
        return this;
    }

    private Predicate extractPredicate(List<Parameter> parameterList, BinaryOperator<Predicate> accumulator) {

        if (parameterList == null) {
            return (p) -> true;
        }

        Map<String, List<Parameter>> mapGroupedByFieldName = parameterList.
                stream().
                collect(Collectors.groupingBy(Parameter::getFieldName));

        return mapGroupedByFieldName.entrySet().
                parallelStream().
                filter(entry -> !entry.getValue().isEmpty()).
                map(entry -> new FieldValuePredicate(entry.getKey(), entry.getValue().stream().map(Parameter::getValue).collect(Collectors.toList()))).
                map(p -> (Predicate) p).
                reduce(accumulator).
                orElse((p) -> true);
    }

    @RequiredArgsConstructor
    private class FieldValuePredicate implements Predicate {

        private final String fieldName;

        private final List<Parameter.Value> values;

        @Override
        public boolean test(Object bean) {
            try {
                Comparable beanValue = getValue(bean, fieldName);
                return values.stream().
                        filter(p -> p.getClause().test(beanValue, p.getValue())).
                        findAny().
                        isPresent();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private Comparable getValue(Object bean, String fieldName) throws IllegalAccessException, NoSuchFieldException {
            Field field = bean.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (Comparable) field.get(bean);
        }
    }
}

package com.test.rule;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

@SuppressWarnings("unchecked")
public interface IRule extends Predicate, Consumer<Element>, Comparable<IRule> {

    IRule withPriority(Integer priority);

    IRule withName(String name);

    IRule withDescription(String description);

    IRule withRejectionCode(String rejectionCode);

    IRule withParameter(List<Parameter> parameters);

    Integer getPriority();

    String getName();

    String getDescription();

    String getRejectionCode();

    Predicate getKeyPredicate();

    Predicate getValuePredicate();

    @Override
    default int compareTo(IRule rule) {
        return this.getPriority().compareTo(rule.getPriority());
    }

    @Override
    default void accept(Element element) {
        boolean isAccepted = test(element.getBean());
        if (!isAccepted){
            element.getRejectionCodes().add(getRejectionCode());
            element.setStatus(Element.Status.REJECTED);
        }
    }

    @Override
    default boolean test(Object bean) {
        // If bean is accepted by key predicate then test value predicate. Else return true (this bean is not rejected by this rule)
        return getKeyPredicate().test(bean) ? getValuePredicate().test(bean) : true;
    }
}

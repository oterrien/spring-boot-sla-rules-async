package com.test.service;

import com.test.exchange.Request;
import com.test.exchange.Response;
import com.test.invoice.Invoice;
import com.test.rule.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@Slf4j
@SuppressWarnings("unchecked")
public class RuleService {

    public Response applyRules(Request request) {

        wait_(request.getEntities().size());

        Response response = new Response();

        // chain all rules sorted by priority
        Consumer<Element> ruleApplier = chainRule(request.getRules());

        Map<Element.Status, List<Element<Invoice>>> mapResult = request.
                getEntities().
                parallelStream().
                map(Element::new).
                peek(ruleApplier).
                collect(Collectors.groupingBy(Element::getStatus));

        Optional.ofNullable(mapResult.get(Element.Status.ACCEPTED)).
                ifPresent(p -> response.getAcceptedElements().addAll(p.stream().map(Element::getBean).collect(Collectors.toList())));

        Optional.ofNullable(mapResult.get(Element.Status.REJECTED)).
                ifPresent(p -> response.getRejectedElements().addAll(p));

        return response;
    }

    private void wait_(int waitingTime) {
        log.warn(String.format("Waiting for %d", waitingTime));
        try {
            Thread.sleep(waitingTime);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Consumer<Element> chainRule(List<Rule> rules) {

        return rules.
                stream().
                map(this::transform).
                map(r -> (Consumer<Element>) r).
                reduce(Consumer::andThen)
                .get();
    }

    private IRule transform(Rule rule) {
        try {
            String className = rule.getRuleType().getClassName();
            Class clazz = Class.forName(className);

            return IRule.class.cast(clazz.newInstance()).
                    withPriority(rule.getPriority()).
                    withName(rule.getName()).
                    withDescription(rule.getDescription()).
                    withRejectionCode(rule.getRejectionCode().getCode()).
                    withParameter(transform(rule.getRuleParameter()));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<Parameter> transform(List<RuleParameter> ruleParameters) {
        return ruleParameters.stream().map(this::transform).collect(Collectors.toList());
    }

    private Parameter transform(RuleParameter ruleParameter) {
        return Parameter.of(ruleParameter.getField(), ruleParameter.getValue(), ruleParameter.getClause(), ruleParameter.getType(), ruleParameter.isKey());
    }
}

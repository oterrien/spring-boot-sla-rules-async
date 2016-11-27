package com.test.persistence.service;


import com.test.persistence.repository.IRuleRepository;
import com.test.rule.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RulePersistenceService {

    @Autowired
    private IRuleRepository ruleRepository;

    public List<Rule> getAllByPriority() {

        Sort sort = new Sort(Sort.Direction.ASC, "priority");
        return ruleRepository.findAll(sort);
    }

}

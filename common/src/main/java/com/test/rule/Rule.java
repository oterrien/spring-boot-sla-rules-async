package com.test.rule;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "RULE")
public class Rule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private long id;

    @Column(name = "PRIORITY")
    private Integer priority;

    @Column(name = "NAME")
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "REJECTION_CODE_ID")
    private RejectionCode rejectionCode;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "RULE_TYPE_ID")
    private RuleType ruleType;

    @OneToMany(mappedBy = "rule", fetch = FetchType.EAGER)
    private List<RuleParameter> ruleParameter;
}

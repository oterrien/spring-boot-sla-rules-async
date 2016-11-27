package com.test.rule;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "RULE_PARAMETER")
public class RuleParameter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    @JsonIgnore
    private long id;

    @Column(name = "FIELD")
    private String field;

    @Column(name = "VALUE")
    private String value;

    @Column(name = "CLAUSE")
    private String clause;

    @Column(name = "TYPE")
    private String type;

    @Column(name = "IS_KEY")
    private boolean isKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RULE_ID")
    @JsonIgnore
    private Rule rule;
}

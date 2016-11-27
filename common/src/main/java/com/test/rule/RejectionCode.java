package com.test.rule;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "REJECTION_CODE")
public class RejectionCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private long  id;

    @Column(name = "CODE")
    private String code;

    @OneToMany(mappedBy = "rejectionCode", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Rule> rules;
}

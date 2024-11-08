// LoanPolicy.java
package com.example.library.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "loan_policies")
public class LoanPolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String itemType;

    @Column(nullable = false)
    private Integer loanPeriodDays;

    @Column(nullable = false)
    private Integer maxRenewals;

    private Integer gracePeriodDays;

    @Convert(converter = IntegerListConverter.class)
    @Column(nullable = false, columnDefinition = "json")
    private List<Integer> reminderDays;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
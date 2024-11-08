// LoanRenewal.java
package com.example.library.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "loan_renewals")
public class LoanRenewal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(nullable = false)
    private LocalDateTime renewalDate;

    @Column(nullable = false)
    private LocalDate previousDueDate;

    @Column(nullable = false)
    private LocalDate newDueDate;

    private String reason;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    private LocalDateTime createdAt;
}
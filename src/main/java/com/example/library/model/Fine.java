package com.example.library.model;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "fines")
public class Fine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String reason;

    @Column(name = "date_issued", nullable = false)
    private LocalDate dateIssued;

    @Column(name = "date_paid")
    private LocalDate datePaid;

    // Lombok will generate getters, setters, toString, equals, and hashCode methods
}
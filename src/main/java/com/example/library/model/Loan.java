// Loan.java
package com.example.library.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Entity
@Table(name = "loans")
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Fine> fines;

    @Column(nullable = false)
    private LocalDate loanDate;

    @Column(nullable = false)
    private LocalDate dueDate;

    private LocalDate returnDate;

    private Integer renewalCount = 0;

    private LocalDate renewalDueDate;

    private String renewalReason;

    private LocalDateTime lastReminderSent;

    @ManyToOne
    @JoinColumn(name = "loan_policy_id")
    private LoanPolicy loanPolicy;

    public boolean isRenewable() {
        if (returnDate != null || loanPolicy == null) {
            return false;
        }

        if (renewalCount >= loanPolicy.getMaxRenewals()) {
            return false;
        }

        LocalDate effectiveDueDate = renewalDueDate != null ? renewalDueDate : dueDate;
        return LocalDate.now().isBefore(effectiveDueDate.plusDays(loanPolicy.getGracePeriodDays()));
    }

    public boolean isOverdue() {
        if (returnDate != null) {
            return false;
        }
        LocalDate effectiveDueDate = renewalDueDate != null ? renewalDueDate : dueDate;
        return LocalDate.now().isAfter(effectiveDueDate);
    }
}
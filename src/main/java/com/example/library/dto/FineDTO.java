// FineDTO.java
package com.example.library.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FineDTO {
    private Long id;
    // Loan related info
    private Long loanId;
    private LocalDate loanDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    // Book related info
    private String bookId;
    private String bookTitle;
    // User related info
    private Long userId;
    private String userName;
    // Fine specific info
    private BigDecimal amount;
    private String reason;
    private LocalDate dateIssued;
    private LocalDate datePaid;
    private Boolean isPaid;
    private Long daysOverdue;
}
// LoanDTO.java
package com.example.library.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class LoanDTO {
    private Long id;
    private String bookId;
    private String bookTitle;
    private Long userId;
    private String userName;
    private LocalDate loanDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private Boolean isOverdue;
    private Integer renewalCount;
    private LocalDate renewalDueDate;
    private Boolean isRenewable;
    private Integer maxRenewals;
    private String itemType;
    private List<LoanRenewalDTO> renewalHistory;
}
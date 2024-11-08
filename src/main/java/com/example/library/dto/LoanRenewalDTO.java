
// LoanRenewalDTO.java
package com.example.library.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LoanRenewalDTO {
    private LocalDateTime renewalDate;
    private LocalDate previousDueDate;
    private LocalDate newDueDate;
    private String reason;
    private String renewedBy;
}
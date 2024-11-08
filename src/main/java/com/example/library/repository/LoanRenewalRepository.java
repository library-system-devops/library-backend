// LoanRenewalRepository.java
package com.example.library.repository;

import com.example.library.model.LoanRenewal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LoanRenewalRepository extends JpaRepository<LoanRenewal, Long> {
    List<LoanRenewal> findByLoanIdOrderByRenewalDateDesc(Long loanId);
}
// LoanRepository.java
package com.example.library.repository;

import com.example.library.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByUserId(Long userId);
    List<Loan> findByBookId(String bookId);
    List<Loan> findByBookIdAndReturnDateIsNull(String bookId);

    int countByUserIdAndReturnDateIsNull(Long userId);
    int countByReturnDateIsNull();
}
package com.example.library.repository;

import com.example.library.model.Fine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FineRepository extends JpaRepository<Fine, Long> {
    List<Fine> findByLoanId(Long loanId);
    List<Fine> findByLoanUserId(Long userId);
}
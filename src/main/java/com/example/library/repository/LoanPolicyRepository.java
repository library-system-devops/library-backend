// LoanPolicyRepository.java
package com.example.library.repository;

import com.example.library.model.LoanPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface LoanPolicyRepository extends JpaRepository<LoanPolicy, Long> {
    Optional<LoanPolicy> findByItemType(String itemType);
}
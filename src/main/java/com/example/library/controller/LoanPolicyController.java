// LoanPolicyController.java
package com.example.library.controller;

import com.example.library.model.LoanPolicy;
import com.example.library.repository.LoanPolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loan-policies")
public class LoanPolicyController {

    @Autowired
    private LoanPolicyRepository loanPolicyRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public List<LoanPolicy> getAllPolicies() {
        return loanPolicyRepository.findAll();
    }

    @GetMapping("/{itemType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<LoanPolicy> getPolicyByType(@PathVariable String itemType) {
        return loanPolicyRepository.findByItemType(itemType)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
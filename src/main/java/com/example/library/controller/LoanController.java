// LoanController.java
package com.example.library.controller;

import com.example.library.dto.LoanDTO;
import com.example.library.dto.LoanRenewalDTO;
import com.example.library.model.User;
import com.example.library.service.LoanService;
import com.example.library.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {
    @Autowired
    private LoanService loanService;

    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public List<LoanDTO> getAllLoans() {
        return loanService.getAllLoansDTO();
    }

    @GetMapping("/my-loans")
    @PreAuthorize("isAuthenticated()")
    public List<LoanDTO> getMyLoans() {
        User currentUser = userService.getCurrentUser();
        return loanService.getLoansDTOByUserId(currentUser.getId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoanDTO> getLoanById(@PathVariable Long id) {
        return loanService.getLoanDTOById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public List<LoanDTO> getLoansByUserId(@PathVariable Long userId) {
        return loanService.getLoansDTOByUserId(userId);
    }

    @GetMapping("/book/{bookId}")
    public List<LoanDTO> getLoansByBookId(@PathVariable String bookId) {
        return loanService.getLoansDTOByBookId(bookId);
    }

    @GetMapping("/activeCount")
    @PreAuthorize("hasRole('ADMIN')")
    public int getActiveLoanCount() {
        return loanService.getActiveLoanCount();
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<?> checkoutBook(
            @RequestParam String bookId,
            @RequestParam Long userId) {
        try {
            LoanDTO loan = loanService.checkoutBook(bookId, userId);
            return ResponseEntity.ok(loan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<?> returnBook(@PathVariable Long id) {
        try {
            LoanDTO loan = loanService.returnBook(id);
            return ResponseEntity.ok(loan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/renew")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN') or @loanService.isUserLoan(#id, authentication.name)")
    public ResponseEntity<?> renewLoan(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        try {
            LoanDTO loan = loanService.renewLoan(id, reason);
            return ResponseEntity.ok(loan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN') or @loanService.isUserLoan(#id, authentication.name)")
    public ResponseEntity<?> getLoanHistory(@PathVariable Long id) {
        try {
            List<LoanRenewalDTO> history = loanService.getLoanRenewalHistory(id);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/activeCount")
    @PreAuthorize("@userService.getUserById(#id).get().username == authentication.name")
    public int getActiveLoanCountByUser(@PathVariable Long id) {
        return loanService.getActiveLoanCountByUser(id);
    }
}
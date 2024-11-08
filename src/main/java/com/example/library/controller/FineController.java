// FineController.java
package com.example.library.controller;

import com.example.library.dto.FineDTO;
import com.example.library.service.FineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fines")
public class FineController {

    @Autowired
    private FineService fineService;

    @GetMapping
    public List<FineDTO> getAllFines() {
        return fineService.getAllFinesDTO();
    }

    @GetMapping("/{id}")
    public ResponseEntity<FineDTO> getFineById(@PathVariable Long id) {
        return fineService.getFineDTOById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/loan/{loanId}")
    public List<FineDTO> getFinesByLoanId(@PathVariable Long loanId) {
        return fineService.getFinesDTOByLoanId(loanId);
    }

    @GetMapping("/user/{userId}")
    public List<FineDTO> getFinesByUserId(@PathVariable Long userId) {
        return fineService.getFinesDTOByUserId(userId);
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<?> payFine(@PathVariable Long id) {
        try {
            FineDTO fine = fineService.payFine(id);
            return ResponseEntity.ok(fine);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
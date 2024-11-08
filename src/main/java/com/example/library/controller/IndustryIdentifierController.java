package com.example.library.controller;

import com.example.library.model.IndustryIdentifier;
import com.example.library.service.IndustryIdentifierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/industry-identifiers")
public class IndustryIdentifierController {

    @Autowired
    private IndustryIdentifierService industryIdentifierService;

    @GetMapping
    public List<IndustryIdentifier> getAllIndustryIdentifiers() {
        return industryIdentifierService.getAllIndustryIdentifiers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<IndustryIdentifier> getIndustryIdentifierById(@PathVariable Long id) {
        return industryIdentifierService.getIndustryIdentifierById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/book/{bookId}")
    public List<IndustryIdentifier> getIndustryIdentifiersByBookId(@PathVariable String bookId) {
        return industryIdentifierService.getIndustryIdentifiersByBookId(bookId);
    }

    @GetMapping("/type/{type}")
    public List<IndustryIdentifier> getIndustryIdentifiersByType(@PathVariable String type) {
        return industryIdentifierService.getIndustryIdentifiersByType(type);
    }

    @PostMapping
    public IndustryIdentifier createIndustryIdentifier(@RequestBody IndustryIdentifier industryIdentifier) {
        return industryIdentifierService.saveIndustryIdentifier(industryIdentifier);
    }

    @PutMapping("/{id}")
    public ResponseEntity<IndustryIdentifier> updateIndustryIdentifier(@PathVariable Long id, @RequestBody IndustryIdentifier industryIdentifier) {
        if (!industryIdentifierService.getIndustryIdentifierById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        industryIdentifier.setId(id);
        return ResponseEntity.ok(industryIdentifierService.saveIndustryIdentifier(industryIdentifier));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIndustryIdentifier(@PathVariable Long id) {
        if (!industryIdentifierService.getIndustryIdentifierById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        industryIdentifierService.deleteIndustryIdentifier(id);
        return ResponseEntity.ok().build();
    }
}
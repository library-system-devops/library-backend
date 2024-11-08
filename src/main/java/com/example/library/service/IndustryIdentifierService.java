package com.example.library.service;

import com.example.library.model.IndustryIdentifier;
import com.example.library.repository.IndustryIdentifierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class IndustryIdentifierService {

    @Autowired
    private IndustryIdentifierRepository industryIdentifierRepository;

    public List<IndustryIdentifier> getAllIndustryIdentifiers() {
        return industryIdentifierRepository.findAll();
    }

    public Optional<IndustryIdentifier> getIndustryIdentifierById(Long id) {
        return industryIdentifierRepository.findById(id);
    }

    public List<IndustryIdentifier> getIndustryIdentifiersByBookId(String bookId) {
        return industryIdentifierRepository.findByBookId(bookId);
    }

    public List<IndustryIdentifier> getIndustryIdentifiersByType(String type) {
        return industryIdentifierRepository.findByType(type);
    }

    public IndustryIdentifier saveIndustryIdentifier(IndustryIdentifier industryIdentifier) {
        return industryIdentifierRepository.save(industryIdentifier);
    }

    public void deleteIndustryIdentifier(Long id) {
        industryIdentifierRepository.deleteById(id);
    }
}
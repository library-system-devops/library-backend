package com.example.library.repository;

import com.example.library.model.IndustryIdentifier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IndustryIdentifierRepository extends JpaRepository<IndustryIdentifier, Long> {
    List<IndustryIdentifier> findByBookId(String bookId);
    List<IndustryIdentifier> findByType(String type);
}
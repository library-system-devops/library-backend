// AuthorService.java
package com.example.library.service;

import com.example.library.dto.AuthorDTO;
import com.example.library.model.Author;
import com.example.library.model.Book;
import com.example.library.repository.AuthorRepository;
import com.example.library.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthorService {
    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    public List<Author> getAllAuthors() {
        return authorRepository.findAll();
    }

    public List<AuthorDTO> getAllAuthorsDTO() {
        return authorRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<Author> getAuthorById(Long id) {
        return authorRepository.findById(id);
    }

    public Optional<AuthorDTO> getAuthorDTOById(Long id) {
        return authorRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Transactional
    public AuthorDTO saveAuthorDTO(AuthorDTO authorDTO) {
        Author author = convertToEntity(authorDTO);
        Author savedAuthor = authorRepository.save(author);
        return convertToDTO(savedAuthor);
    }

    public void deleteAuthor(Long id) {
        authorRepository.deleteById(id);
    }

    private AuthorDTO convertToDTO(Author author) {
        AuthorDTO dto = new AuthorDTO();
        dto.setId(author.getId());
        dto.setName(author.getName());
        dto.setBookIds(author.getBooks().stream()
                .map(Book::getId)
                .collect(Collectors.toSet()));
        return dto;
    }

    private Author convertToEntity(AuthorDTO dto) {
        Author author = new Author();
        author.setId(dto.getId());
        author.setName(dto.getName());

        if (dto.getBookIds() != null) {
            Set<Book> books = dto.getBookIds().stream()
                    .map(bookId -> bookRepository.findById(bookId).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            author.setBooks(books);
        }

        return author;
    }
}
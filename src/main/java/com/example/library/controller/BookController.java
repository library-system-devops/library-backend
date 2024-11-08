package com.example.library.controller;

import com.example.library.dto.BookDTO;
import com.example.library.dto.GoogleBooksDTO;
import com.example.library.dto.GoogleBooksSearchResultDTO;
import com.example.library.exceptions.BookDeleteException;
import com.example.library.exceptions.BookNotFoundException;
import com.example.library.exceptions.InvalidInventoryUpdateException;
import com.example.library.service.BookService;
import com.example.library.service.GoogleBooksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {
    @Autowired
    private BookService bookService;

    @Autowired
    private GoogleBooksService googleBooksService;

    @GetMapping
    public List<BookDTO> getAllBooks() {
        return bookService.getAllBooksDTO();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDTO> getBookById(@PathVariable String id) {
        return bookService.getBookDTOById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN')")
    public int getBookCount() {
        return bookService.getBookCount();
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookDTO> updateBook(@PathVariable String id, @RequestBody BookDTO bookDTO) {
        if (!bookService.getBookById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        bookDTO.setId(id);
        return ResponseEntity.ok(bookService.saveBookDTO(bookDTO));
    }

    @PutMapping("/{id}/inventory")
    public ResponseEntity<BookDTO> updateBookInventory(
            @PathVariable String id,
            @RequestParam int newCopiesOwned) {
        try {
            BookDTO updatedBook = bookService.updateBookInventory(id, newCopiesOwned);
            return ResponseEntity.ok(updatedBook);
        } catch (BookNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (InvalidInventoryUpdateException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Google Books Integration endpoints
    @GetMapping("/search")
    public ResponseEntity<GoogleBooksSearchResultDTO> searchBooks(@RequestParam String query) {
        try {
            GoogleBooksSearchResultDTO searchResult = googleBooksService.searchBooks(query);
            return ResponseEntity.ok(searchResult);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/google")
    public ResponseEntity<BookDTO> createBookFromGoogle(@RequestBody GoogleBooksDTO googleBook) {
        try {
            BookDTO savedBook = bookService.createBookFromGoogleBooks(googleBook);
            return ResponseEntity.ok(savedBook);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(null);
        }
    }

    // Exception handlers
    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<String> handleBookNotFound(BookNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(BookDeleteException.class)
    public ResponseEntity<String> handleBookDeleteError(BookDeleteException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
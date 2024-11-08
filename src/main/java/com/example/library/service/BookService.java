// BookService.java
package com.example.library.service;

import com.example.library.dto.BookDTO;
import com.example.library.dto.BookInventoryStatusDTO;
import com.example.library.dto.GoogleBooksDTO;
import com.example.library.exceptions.BookDeleteException;
import com.example.library.exceptions.BookNotFoundException;
import com.example.library.exceptions.InvalidInventoryUpdateException;
import com.example.library.model.*;
import com.example.library.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookService {
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private IndustryIdentifierRepository industryIdentifierRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    // Basic CRUD Methods
    public List<BookDTO> getAllBooksDTO() {
        return bookRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<BookDTO> getBookDTOById(String id) {
        return bookRepository.findById(id)
                .map(this::convertToDTO);
    }

    public Optional<Book> getBookById(String id) {
        return bookRepository.findById(id);
    }

    @Transactional
    public BookDTO saveBookDTO(BookDTO bookDTO) {
        Book book = convertToEntity(bookDTO);

        // If this is an update, fetch the existing book first
        if (book.getId() != null) {
            Book existingBook = bookRepository.findById(book.getId())
                    .orElseThrow(() -> new RuntimeException("Book not found"));

            // Preserve or update authors
            if (bookDTO.getAuthors() != null) {
                Set<Author> authors = new HashSet<>();
                for (String authorName : bookDTO.getAuthors()) {
                    Author author = authorRepository.findByName(authorName)
                            .orElseGet(() -> {
                                Author newAuthor = new Author();
                                newAuthor.setName(authorName);
                                return authorRepository.save(newAuthor);
                            });
                    authors.add(author);
                }
                book.setAuthors(authors);
            } else {
                // Keep existing authors if none provided
                book.setAuthors(existingBook.getAuthors());
            }

            // Preserve or update categories
            if (bookDTO.getCategories() != null) {
                Set<Category> categories = new HashSet<>();
                for (String categoryName : bookDTO.getCategories()) {
                    Category category = categoryRepository.findByName(categoryName)
                            .orElseGet(() -> {
                                Category newCategory = new Category();
                                newCategory.setName(categoryName);
                                return categoryRepository.save(newCategory);
                            });
                    categories.add(category);
                }
                book.setCategories(categories);
            } else {
                // Keep existing categories if none provided
                book.setCategories(existingBook.getCategories());
            }
        }

        Book savedBook = bookRepository.save(book);
        return convertToDTO(savedBook);
    }

    @Transactional
    public BookDTO updateBookInventory(String id, int newCopiesOwned) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));

        // Get count of books currently on loan
        long booksOnLoan = loanRepository.findByBookIdAndReturnDateIsNull(id).size();

        // Validate the new inventory level
        if (newCopiesOwned < booksOnLoan) {
            throw new InvalidInventoryUpdateException(
                    "Cannot reduce copies owned below number of books currently on loan (" + booksOnLoan + ")"
            );
        }

        // Calculate new copies available
        int newCopiesAvailable = newCopiesOwned - (int)booksOnLoan;

        // Update the book
        book.setCopiesOwned(newCopiesOwned);
        book.setCopiesAvailable(newCopiesAvailable);

        Book updatedBook = bookRepository.save(book);
        return convertToDTO(updatedBook);
    }

    // Helper method to get current inventory status
    public BookInventoryStatusDTO getBookInventoryStatus(String id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));

        long booksOnLoan = loanRepository.findByBookIdAndReturnDateIsNull(id).size();
        long activeReservations = reservationRepository
                .findByBookIdAndStatus(id, Reservation.Status.ACTIVE).size();

        return BookInventoryStatusDTO.builder()
                .copiesOwned(book.getCopiesOwned())
                .copiesAvailable(book.getCopiesAvailable())
                .copiesOnLoan(booksOnLoan)
                .activeReservations(activeReservations)
                .minimumCopiesRequired(booksOnLoan)
                .build();
    }

    // Google Books Integration
    @Transactional
    public BookDTO createBookFromGoogleBooks(GoogleBooksDTO googleBook) {
        // Check if book already exists
        if (bookRepository.findById(googleBook.getId()).isPresent()) {
            throw new RuntimeException("Book already exists in the database");
        }

        // Create new book
        Book book = new Book();
        book.setId(googleBook.getId());
        book.setTitle(googleBook.getVolumeInfo().getTitle());
        book.setPublishedDate(googleBook.getVolumeInfo().getPublishedDate());
        book.setDescription(googleBook.getVolumeInfo().getDescription());
        book.setAverageRating(googleBook.getVolumeInfo().getAverageRating());
        book.setRatingsCount(googleBook.getVolumeInfo().getRatingsCount());
        book.setThumbnailUrl(googleBook.getVolumeInfo().getImageLinks() != null ?
                googleBook.getVolumeInfo().getImageLinks().getThumbnail() : null);
        book.setCopiesOwned(1);
        book.setCopiesAvailable(1);
        book.setPolicyType("BOOK"); // Default policy type

        // Save the book first to establish the ID
        Book savedBook = bookRepository.save(book);

        // Handle authors
        if (googleBook.getVolumeInfo().getAuthors() != null) {
            Set<Author> authors = new HashSet<>();
            for (String authorName : googleBook.getVolumeInfo().getAuthors()) {
                Author author = authorRepository.findByName(authorName)
                        .orElseGet(() -> {
                            Author newAuthor = new Author();
                            newAuthor.setName(authorName);
                            return authorRepository.save(newAuthor);
                        });

                // Add book to author's books
                if (author.getBooks() == null) {
                    author.setBooks(new HashSet<>());
                }
                author.getBooks().add(savedBook);

                // Add author to book's authors
                authors.add(author);

                // Save the updated author
                authorRepository.save(author);
            }

            // Set and save the book with its authors
            savedBook.setAuthors(authors);
            savedBook = bookRepository.save(savedBook);
        }

        // Handle categories
        if (googleBook.getVolumeInfo().getCategories() != null) {
            Set<Category> categories = new HashSet<>();
            for (String categoryName : googleBook.getVolumeInfo().getCategories()) {
                Category category = categoryRepository.findByName(categoryName)
                        .orElseGet(() -> {
                            Category newCategory = new Category();
                            newCategory.setName(categoryName);
                            return categoryRepository.save(newCategory);
                        });
                categories.add(category);
            }
            savedBook.setCategories(categories);
            savedBook = bookRepository.save(savedBook);
        }

        // Handle industry identifiers
        if (googleBook.getVolumeInfo().getIndustryIdentifiers() != null) {
            for (GoogleBooksDTO.IndustryIdentifier identifier :
                    googleBook.getVolumeInfo().getIndustryIdentifiers()) {
                IndustryIdentifier industryIdentifier = new IndustryIdentifier();
                industryIdentifier.setBook(savedBook);
                industryIdentifier.setType(identifier.getType());
                industryIdentifier.setIdentifier(identifier.getIdentifier());
                industryIdentifierRepository.save(industryIdentifier);
            }
        }

        return convertToDTO(savedBook);
    }

    // Conversion Methods
    private BookDTO convertToDTO(Book book) {
        BookDTO dto = new BookDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setPublishedDate(book.getPublishedDate());
        dto.setDescription(book.getDescription());
        dto.setAverageRating(book.getAverageRating());
        dto.setRatingsCount(book.getRatingsCount());
        dto.setThumbnailUrl(book.getThumbnailUrl());
        dto.setCopiesOwned(book.getCopiesOwned());
        dto.setCopiesAvailable(book.getCopiesAvailable());
        dto.setPolicyType(book.getPolicyType());

        if (book.getAuthors() != null) {
            dto.setAuthors(book.getAuthors().stream()
                    .map(Author::getName)
                    .collect(Collectors.toSet()));
        } else {
            dto.setAuthors(new HashSet<>());
        }

        if (book.getCategories() != null) {
            dto.setCategories(book.getCategories().stream()
                    .map(Category::getName)
                    .collect(Collectors.toSet()));
        } else {
            dto.setCategories(new HashSet<>());
        }

        return dto;
    }

    private Book convertToEntity(BookDTO dto) {
        Book book = new Book();
        book.setId(dto.getId());
        book.setTitle(dto.getTitle());
        book.setPublishedDate(dto.getPublishedDate());
        book.setDescription(dto.getDescription());
        book.setAverageRating(dto.getAverageRating());
        book.setRatingsCount(dto.getRatingsCount());
        book.setThumbnailUrl(dto.getThumbnailUrl());
        book.setCopiesOwned(dto.getCopiesOwned());
        book.setCopiesAvailable(dto.getCopiesAvailable());
        book.setPolicyType(dto.getPolicyType());
        book.setAuthors(new HashSet<>());  // Initialize empty sets
        book.setCategories(new HashSet<>());
        return book;
    }

    public int getBookCount() {

        return bookRepository.findAll()
                .stream()
                .mapToInt(Book::getCopiesOwned)
                .sum();

    }
}
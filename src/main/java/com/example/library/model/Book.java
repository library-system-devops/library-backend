// Book.java
package com.example.library.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "books")
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"authors", "categories", "industryIdentifiers"})
public class Book {
    @Id
    private String id;

    @Column(nullable = false)
    private String title;

    private String publishedDate;

    @Column(columnDefinition = "LONGTEXT")
    private String description;

    private Double averageRating;
    private Integer ratingsCount;
    private String thumbnailUrl;
    private Integer copiesOwned;
    private Integer copiesAvailable;

    @Column(nullable = false)
    private String policyType = "BOOK";  // Add this field with default value

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "book_authors",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    @JsonManagedReference
    private Set<Author> authors = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "book_categories",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @JsonManagedReference
    private Set<Category> categories = new HashSet<>();

    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    @JsonManagedReference
    private Set<IndustryIdentifier> industryIdentifiers = new HashSet<>();
}
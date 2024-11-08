// IndustryIdentifier.java
package com.example.library.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "industry_identifiers")
@EqualsAndHashCode(of = "id")
@ToString(exclude = "book")
public class IndustryIdentifier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    @JsonBackReference
    private Book book;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String identifier;
}
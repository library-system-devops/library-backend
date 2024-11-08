// ReservationService.java
package com.example.library.service;

import com.example.library.dto.ReservationDTO;
import com.example.library.model.*;
import com.example.library.repository.ReservationRepository;
import com.example.library.repository.BookRepository;
import com.example.library.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReservationService {
    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(ReservationService.class);

    public List<ReservationDTO> getAllReservationsDTO() {
        return reservationRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<ReservationDTO> getReservationDTOById(Long id) {
        return reservationRepository.findById(id)
                .map(this::convertToDTO);
    }

    public List<ReservationDTO> getReservationsDTOByUserId(Long userId) {
        return reservationRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ReservationDTO> getReservationsDTOByBookId(String bookId) {
        return reservationRepository.findByBookId(bookId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ReservationDTO convertToDTO(Reservation reservation) {
        ReservationDTO dto = new ReservationDTO();
        dto.setId(reservation.getId());
        dto.setBookId(reservation.getBook().getId());
        dto.setBookTitle(reservation.getBook().getTitle());
        dto.setUserId(reservation.getUser().getId());
        dto.setUserName(reservation.getUser().getFirstName() + " " + reservation.getUser().getLastName());
        dto.setReservationDate(reservation.getReservationDate());
        dto.setExpirationDate(reservation.getExpirationDate());
        dto.setStatus(reservation.getStatus());

        // Calculate queue position if status is ACTIVE
        if (reservation.getStatus() == Reservation.Status.ACTIVE) {
            dto.setQueuePosition(calculateQueuePosition(reservation));
        }

        return dto;
    }

    private Integer calculateQueuePosition(Reservation reservation) {
        List<Reservation> activeReservations = reservationRepository
                .findByBookIdAndStatusOrderByReservationDateAsc(
                        reservation.getBook().getId(),
                        Reservation.Status.ACTIVE
                );

        for (int i = 0; i < activeReservations.size(); i++) {
            if (activeReservations.get(i).getId().equals(reservation.getId())) {
                return i + 1;
            }
        }
        return null;
    }

    public boolean hasActiveReservations(String bookId) {
        return reservationRepository.countByBookIdAndStatus(
                bookId,
                Reservation.Status.ACTIVE
        ) > 0;
    }

    @Transactional
    public ReservationDTO reserveBook(String bookId, Long userId) throws Exception {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new Exception("Book not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        if (book.getCopiesAvailable() > 0) {
            throw new Exception("Book is available for immediate checkout, no need for reservation");
        }

        // Check if user already has an active reservation for this book
        Optional<Reservation> existingReservation = reservationRepository
                .findByBookIdAndUserIdAndStatus(bookId, userId, Reservation.Status.ACTIVE);
        if (existingReservation.isPresent()) {
            throw new Exception("You already have an active reservation for this book");
        }

        Reservation reservation = new Reservation();
        reservation.setBook(book);
        reservation.setUser(user);
        reservation.setReservationDate(LocalDateTime.now());
        reservation.setExpirationDate(LocalDateTime.now().plusDays(7));
        reservation.setStatus(Reservation.Status.ACTIVE);

        Reservation savedReservation = reservationRepository.save(reservation);
        return convertToDTO(savedReservation);
    }

    @Transactional
    public void processNextReservation(String bookId) {
        // Get the book
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        // Only process if there are available copies
        if (book.getCopiesAvailable() <= 0) {
            return;
        }

        // Get the next active reservation in queue
        List<Reservation> activeReservations = reservationRepository
                .findByBookIdAndStatusOrderByReservationDateAsc(bookId, Reservation.Status.ACTIVE);

        if (!activeReservations.isEmpty()) {
            Reservation nextReservation = activeReservations.get(0);

            // Update reservation status
            nextReservation.setStatus(Reservation.Status.FULFILLED);
            reservationRepository.save(nextReservation);

            // Log notification (in a real system, this would send an email/SMS)
            logger.info("NOTIFICATION: Book '{}' is now available for user '{}'. Reservation ID: {}. " +
                            "The book will be held for 48 hours.",
                    book.getTitle(),
                    nextReservation.getUser().getUsername(),
                    nextReservation.getId());
        }
    }

    // Modify the existing fulfillReservation method to be internal only
    @Deprecated
    public Optional<ReservationDTO> fulfillReservation(Long id) {
        throw new UnsupportedOperationException(
                "Manual fulfillment is deprecated. Reservations are automatically fulfilled when books are returned."
        );
    }

    public int getReservationCountByUserId(Long id) {
        return reservationRepository.countByUserId(id);
    }
}
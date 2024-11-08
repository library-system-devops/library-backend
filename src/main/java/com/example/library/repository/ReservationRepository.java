
// ReservationRepository.java
package com.example.library.repository;

import com.example.library.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserId(Long userId);
    List<Reservation> findByBookId(String bookId);
    Optional<Reservation> findByBookIdAndUserIdAndStatus(String bookId, Long userId, Reservation.Status status);
    List<Reservation> findByBookIdAndStatus(String bookId, Reservation.Status status);
    List<Reservation> findByBookIdAndStatusOrderByReservationDateAsc(String bookId, Reservation.Status status);
    long countByBookIdAndStatus(String bookId, Reservation.Status status);

    int countByUserId(Long id);
}
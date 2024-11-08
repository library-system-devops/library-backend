// ReservationController.java
package com.example.library.controller;

import com.example.library.dto.ReservationDTO;
import com.example.library.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @GetMapping
    public List<ReservationDTO> getAllReservations() {
        return reservationService.getAllReservationsDTO();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationDTO> getReservationById(@PathVariable Long id) {
        return reservationService.getReservationDTOById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public List<ReservationDTO> getReservationsByUserId(@PathVariable Long userId) {
        return reservationService.getReservationsDTOByUserId(userId);
    }

    @GetMapping("/book/{bookId}")
    public List<ReservationDTO> getReservationsByBookId(@PathVariable String bookId) {
        return reservationService.getReservationsDTOByBookId(bookId);
    }

    @PostMapping("/reserve")
    public ResponseEntity<?> reserveBook(@RequestParam String bookId, @RequestParam Long userId) {
        try {
            ReservationDTO reservation = reservationService.reserveBook(bookId, userId);
            return ResponseEntity.ok(reservation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/fulfill")
    @Deprecated
    public ResponseEntity<?> fulfillReservation(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body("Manual fulfillment is no longer supported. Reservations are automatically fulfilled when books are returned.");
    }

    @GetMapping("/{id}/countByUser")
    @PreAuthorize("@userService.getUserById(#id).get().username == authentication.name")
    public int getReservationCountByUserId(@PathVariable Long id) {
        return reservationService.getReservationCountByUserId(id);
    }
}
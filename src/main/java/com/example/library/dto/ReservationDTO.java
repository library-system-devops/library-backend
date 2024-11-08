// ReservationDTO.java
package com.example.library.dto;

import com.example.library.model.Reservation;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReservationDTO {
    private Long id;
    private String bookId;
    private String bookTitle;
    private Long userId;
    private String userName;
    private LocalDateTime reservationDate;
    private LocalDateTime expirationDate;
    private Reservation.Status status;
    private Integer queuePosition; // Optional: to show position in reservation queue
}

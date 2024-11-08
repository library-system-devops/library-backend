// FineService.java
package com.example.library.service;

import com.example.library.dto.FineDTO;
import com.example.library.model.*;
import com.example.library.repository.FineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FineService {
    @Autowired
    private FineRepository fineRepository;

    @Autowired
    private ReservationService reservationService;

    public List<FineDTO> getAllFinesDTO() {
        return fineRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<FineDTO> getFineDTOById(Long id) {
        return fineRepository.findById(id)
                .map(this::convertToDTO);
    }

    public List<FineDTO> getFinesDTOByLoanId(Long loanId) {
        return fineRepository.findByLoanId(loanId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<FineDTO> getFinesDTOByUserId(Long userId) {
        return fineRepository.findByLoanUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private FineDTO convertToDTO(Fine fine) {
        FineDTO dto = new FineDTO();
        dto.setId(fine.getId());

        // Loan information
        Loan loan = fine.getLoan();
        dto.setLoanId(loan.getId());
        dto.setLoanDate(loan.getLoanDate());
        dto.setDueDate(loan.getDueDate());
        dto.setReturnDate(loan.getReturnDate());

        // Book information
        dto.setBookId(loan.getBook().getId());
        dto.setBookTitle(loan.getBook().getTitle());

        // User information
        dto.setUserId(loan.getUser().getId());
        dto.setUserName(loan.getUser().getFirstName() + " " + loan.getUser().getLastName());

        // Fine specific information
        dto.setAmount(fine.getAmount());
        dto.setReason(fine.getReason());
        dto.setDateIssued(fine.getDateIssued());
        dto.setDatePaid(fine.getDatePaid());
        dto.setIsPaid(fine.getDatePaid() != null);

        // Calculate days overdue
        if (loan.getReturnDate() != null) {
            dto.setDaysOverdue(ChronoUnit.DAYS.between(loan.getDueDate(), loan.getReturnDate()));
        } else {
            dto.setDaysOverdue(ChronoUnit.DAYS.between(loan.getDueDate(), LocalDate.now()));
        }

        return dto;
    }

    @Transactional
    public FineDTO createFineForOverdueBook(Loan loan) {
        LocalDate dueDate = loan.getDueDate();
        LocalDate returnDate = loan.getReturnDate() != null ? loan.getReturnDate() : LocalDate.now();

        long daysOverdue = ChronoUnit.DAYS.between(dueDate, returnDate);
        if (daysOverdue <= 0) {
            return null;
        }

        BigDecimal fineAmount = calculateFineAmount(daysOverdue);

        Fine fine = new Fine();
        fine.setLoan(loan);
        fine.setAmount(fineAmount);
        fine.setReason("Overdue book - " + daysOverdue + " days late");
        fine.setDateIssued(LocalDate.now());

        Fine savedFine = fineRepository.save(fine);

        // After creating fine, check for reservations
        reservationService.processNextReservation(loan.getBook().getId());

        return convertToDTO(savedFine);
    }

    private BigDecimal calculateFineAmount(long daysOverdue) {
        BigDecimal ratePerDay = new BigDecimal("0.50");
        return ratePerDay.multiply(BigDecimal.valueOf(daysOverdue));
    }

    @Transactional
    public FineDTO payFine(Long fineId) throws Exception {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new Exception("Fine not found"));

        if (fine.getDatePaid() != null) {
            throw new Exception("This fine has already been paid");
        }

        fine.setDatePaid(LocalDate.now());
        Fine savedFine = fineRepository.save(fine);
        return convertToDTO(savedFine);
    }
}
// LoanService.java
package com.example.library.service;

import com.example.library.dto.LoanDTO;
import com.example.library.dto.LoanRenewalDTO;
import com.example.library.model.*;
import com.example.library.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LoanService {
    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoanRenewalRepository loanRenewalRepository;

    @Autowired
    private LoanPolicyRepository loanPolicyRepository;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private FineService fineService;

    @Autowired
    private UserService userService;

    public List<LoanDTO> getAllLoansDTO() {
        return loanRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<LoanDTO> getLoanDTOById(Long id) {
        return loanRepository.findById(id)
                .map(this::convertToDTO);
    }

    public List<LoanDTO> getLoansDTOByUserId(Long userId) {
        return loanRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<LoanDTO> getLoansDTOByBookId(String bookId) {
        return loanRepository.findByBookId(bookId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public LoanDTO renewLoan(Long loanId, String reason) throws Exception {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new Exception("Loan not found"));

        validateRenewal(loan);

        User currentUser = userService.getCurrentUser();
        LocalDate previousDueDate = loan.getRenewalDueDate() != null ?
                loan.getRenewalDueDate() : loan.getDueDate();

        // Calculate new due date
        LocalDate newDueDate = LocalDate.now()
                .plusDays(loan.getLoanPolicy().getLoanPeriodDays());

        // Update loan
        loan.setRenewalCount(loan.getRenewalCount() + 1);
        loan.setRenewalDueDate(newDueDate);
        loan.setRenewalReason(reason);

        // Create renewal record
        LoanRenewal renewal = new LoanRenewal();
        renewal.setLoan(loan);
        renewal.setRenewalDate(LocalDateTime.now());
        renewal.setPreviousDueDate(previousDueDate);
        renewal.setNewDueDate(newDueDate);
        renewal.setReason(reason);
        renewal.setCreatedBy(currentUser);

        // Save both
        loanRenewalRepository.save(renewal);
        Loan savedLoan = loanRepository.save(loan);

        return convertToDTO(savedLoan);
    }

    @Transactional
    public LoanDTO returnBook(Long loanId) throws Exception {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new Exception("Loan not found"));

        if (loan.getReturnDate() != null) {
            throw new Exception("This book has already been returned");
        }

        loan.setReturnDate(LocalDate.now());
        String bookId = loan.getBook().getId();

        // Update book availability
        Book book = loan.getBook();
        synchronized(book) {
            book.setCopiesAvailable(calculateAvailableCopies(book));
            bookRepository.save(book);
        }

        // Check for overdue and create fine if necessary
        if (loan.isOverdue()) {
            fineService.createFineForOverdueBook(loan);
        } else {
            // If no fine needs to be created, process reservations directly
            reservationService.processNextReservation(bookId);
        }

        Loan returnedLoan = loanRepository.save(loan);
        return convertToDTO(returnedLoan);
    }

    @Transactional
    public LoanDTO checkoutBook(String bookId, Long userId) throws Exception {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new Exception("Book not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        // Get policy based on book's policy type
        LoanPolicy policy = loanPolicyRepository.findByItemType(book.getPolicyType())
                .orElseThrow(() -> new Exception("No loan policy found for book type: " + book.getPolicyType()));

        synchronized(book) {
            if (book.getCopiesAvailable() <= 0) {
                throw new Exception("No copies available for checkout");
            }

            Loan loan = new Loan();
            loan.setBook(book);
            loan.setUser(user);
            loan.setLoanDate(LocalDate.now());
            loan.setDueDate(LocalDate.now().plusDays(policy.getLoanPeriodDays()));
            loan.setLoanPolicy(policy);
            loan.setRenewalCount(0);

            // Update book availability
            book.setCopiesAvailable(calculateAvailableCopies(book) - 1);
            bookRepository.save(book);

            return convertToDTO(loanRepository.save(loan));
        }
    }

    private int calculateAvailableCopies(Book book) {
        int activeLoans = loanRepository.findByBookIdAndReturnDateIsNull(book.getId()).size();
        return book.getCopiesOwned() - activeLoans;
    }

    private void validateRenewal(Loan loan) throws Exception {
        if (!loan.isRenewable()) {
            throw new Exception("Maximum renewal limit reached");
        }

        if (loan.isOverdue()) {
            throw new Exception("Overdue loans cannot be renewed");
        }

        if (reservationService.hasActiveReservations(loan.getBook().getId())) {
            throw new Exception("Book has active reservations");
        }

        User borrower = loan.getUser();
        if (!borrower.getStatus().equals(User.Status.ACTIVE)) {
            throw new Exception("User account is not active");
        }
    }

    public boolean isUserLoan(Long loanId, String username) {
        return loanRepository.findById(loanId)
                .map(loan -> loan.getUser().getUsername().equals(username))
                .orElse(false);
    }

    public List<LoanRenewalDTO> getLoanRenewalHistory(Long loanId) {
        return loanRenewalRepository.findByLoanIdOrderByRenewalDateDesc(loanId).stream()
                .map(this::convertRenewalToDTO)
                .collect(Collectors.toList());
    }

    private LoanDTO convertToDTO(Loan loan) {
        LoanDTO dto = new LoanDTO();
        dto.setId(loan.getId());
        dto.setBookId(loan.getBook().getId());
        dto.setBookTitle(loan.getBook().getTitle());
        dto.setUserId(loan.getUser().getId());
        dto.setUserName(loan.getUser().getFirstName() + " " + loan.getUser().getLastName());
        dto.setLoanDate(loan.getLoanDate());
        dto.setDueDate(loan.getDueDate());
        dto.setReturnDate(loan.getReturnDate());
        dto.setRenewalCount(loan.getRenewalCount());
        dto.setRenewalDueDate(loan.getRenewalDueDate());
        dto.setIsRenewable(loan.isRenewable());

        if (loan.getLoanPolicy() != null) {
            dto.setMaxRenewals(loan.getLoanPolicy().getMaxRenewals());
            dto.setItemType(loan.getLoanPolicy().getItemType());
        }

        // Calculate if the loan is overdue
        dto.setIsOverdue(loan.isOverdue());

        // Get and set renewal history
        List<LoanRenewal> renewals = loanRenewalRepository
                .findByLoanIdOrderByRenewalDateDesc(loan.getId());

        dto.setRenewalHistory(renewals.stream()
                .map(this::convertRenewalToDTO)
                .collect(Collectors.toList()));

        return dto;
    }

    private LoanRenewalDTO convertRenewalToDTO(LoanRenewal renewal) {
        LoanRenewalDTO dto = new LoanRenewalDTO();
        dto.setRenewalDate(renewal.getRenewalDate());
        dto.setPreviousDueDate(renewal.getPreviousDueDate());
        dto.setNewDueDate(renewal.getNewDueDate());
        dto.setReason(renewal.getReason());
        dto.setRenewedBy(renewal.getCreatedBy().getUsername());
        return dto;
    }

    public int getActiveLoanCount() {
        return loanRepository.countByReturnDateIsNull();
    }

    public int getActiveLoanCountByUser(Long userId) {
        return loanRepository.countByUserIdAndReturnDateIsNull(userId);
    }
}
// Create BookInventoryStatus DTO
package com.example.library.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookInventoryStatusDTO {
    private int copiesOwned;
    private int copiesAvailable;
    private long copiesOnLoan;
    private long activeReservations;
    private long minimumCopiesRequired;
}
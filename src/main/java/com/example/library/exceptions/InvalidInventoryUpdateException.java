package com.example.library.exceptions;

public class InvalidInventoryUpdateException extends RuntimeException {
    public InvalidInventoryUpdateException(String message) {
        super(message);
    }
}
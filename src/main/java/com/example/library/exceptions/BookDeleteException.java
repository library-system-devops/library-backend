package com.example.library.exceptions;

public class BookDeleteException extends RuntimeException {
    public BookDeleteException(String message) {
        super(message);
    }
}
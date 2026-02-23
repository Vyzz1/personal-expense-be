package com.huynh.personal_expense_be.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {


        
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleNotFoundException(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ExceptionResponse.of(ex.getMessage()));

    }

    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<ExceptionResponse> handleDuplicateException(DuplicateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ExceptionResponse.of(ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ExceptionResponse> handleBadRequestException(BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ExceptionResponse.of(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleCommonException(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ExceptionResponse.of(ex.getMessage()));
    }

}
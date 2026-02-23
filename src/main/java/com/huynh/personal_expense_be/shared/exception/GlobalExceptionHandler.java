package com.huynh.personal_expense_be.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // -------------------------------------------------------------------------
    // Domain exceptions
    // -------------------------------------------------------------------------

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleNotFoundException(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ExceptionResponse.of(ex.getMessage()));
    }

    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<ExceptionResponse> handleDuplicateException(DuplicateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ExceptionResponse.of(ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ExceptionResponse> handleBadRequestException(BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ExceptionResponse.of(ex.getMessage()));
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationExceptionResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<ValidationFieldError> fieldErrors = new ArrayList<>();

        ex.getBindingResult().getFieldErrors().forEach(fe ->
                fieldErrors.add(ValidationFieldError.of(
                        fe.getField(),
                        fe.getRejectedValue(),
                        fe.getDefaultMessage(),
                        extractCode(fe.getCodes())
                ))
        );

        ex.getBindingResult().getGlobalErrors().forEach(ge ->
                fieldErrors.add(ValidationFieldError.of(
                        ge.getObjectName(),
                        null,
                        ge.getDefaultMessage(),
                        extractCode(ge.getCodes())
                ))
        );

        return buildValidationResponse(fieldErrors, request.getRequestURI());
    }


    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ValidationExceptionResponse> handleHandlerMethodValidation(
            HandlerMethodValidationException ex, HttpServletRequest request) {

        List<ValidationFieldError> fieldErrors = new ArrayList<>();

        for (ParameterValidationResult pvr : ex.getParameterValidationResults()) {
            if (pvr instanceof ParameterErrors pe) {
                for (FieldError fe : pe.getFieldErrors()) {
                    fieldErrors.add(ValidationFieldError.of(
                            fe.getField(),
                            fe.getRejectedValue(),
                            fe.getDefaultMessage(),
                            extractCode(fe.getCodes())
                    ));
                }
            } else {
                String paramName = pvr.getMethodParameter().getParameterName();
                Object argValue = pvr.getArgument();
                for (MessageSourceResolvable error : pvr.getResolvableErrors()) {
                    fieldErrors.add(ValidationFieldError.of(
                            paramName,
                            argValue,
                            error.getDefaultMessage(),
                            extractCode(error.getCodes())
                    ));
                }
            }
        }

        return buildValidationResponse(fieldErrors, request.getRequestURI());
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationExceptionResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        List<ValidationFieldError> fieldErrors = ex.getConstraintViolations().stream()
                .map(cv -> {
                    String path = cv.getPropertyPath().toString();
                    String field = path.contains(".")
                            ? path.substring(path.lastIndexOf('.') + 1)
                            : path;
                    String code = cv.getConstraintDescriptor()
                            .getAnnotation()
                            .annotationType()
                            .getSimpleName();
                    return ValidationFieldError.of(field, cv.getInvalidValue(), cv.getMessage(), code);
                })
                .toList();

        return buildValidationResponse(fieldErrors, request.getRequestURI());
    }

    // -------------------------------------------------------------------------
    // Catch-all
    // -------------------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleCommonException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception on {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ExceptionResponse.of("An unexpected error occurred. Please try again later."));
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private ResponseEntity<ValidationExceptionResponse> buildValidationResponse(
            List<ValidationFieldError> fieldErrors, String path) {

        int count = fieldErrors.size();
        String message = "Validation failed for %d field%s".formatted(count, count == 1 ? "" : "s");

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ValidationExceptionResponse.of(
                        HttpStatus.UNPROCESSABLE_ENTITY.value(),
                        HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(),
                        message,
                        path,
                        fieldErrors
                ));
    }

    /**
     * Picks the most specific code from the codes array produced by Spring's
     * binding infrastructure (codes are ordered from most-specific to least).
     * Returns the annotation type name only (e.g. "NotBlank" from "NotBlank.user.name").
     */
    private String extractCode(String[] codes) {
        if (codes == null || codes.length == 0) return null;
        String first = codes[0];
        return first.contains(".") ? first.substring(0, first.indexOf('.')) : first;
    }
}
package com.example.ewaste.controller;

import com.example.ewaste.dto.ResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class RestExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(RestExceptionHandler.class);

    // Handle IllegalStateException
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ResponseMessage> handleState(IllegalStateException ex) {
        logger.error("IllegalStateException: ", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseMessage.error(ex.getMessage()));
    }

    // Handle IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseMessage> handleArg(IllegalArgumentException ex) {
        logger.error("IllegalArgumentException: ", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseMessage.error(ex.getMessage()));
    }

    // Handle Authentication failures
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ResponseMessage> handleAuth(AuthenticationException ex) {
        logger.error("AuthenticationException: ", ex);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ResponseMessage.error("Authentication failed: " + ex.getMessage()));
    }

    // Handle validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseMessage> handleValidation(MethodArgumentNotValidException ex) {
        logger.error("ValidationException: ", ex);
        String errorMsg = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseMessage.error(errorMsg));
    }

    // Fallback for any other exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseMessage> handleOther(Exception ex) {
        logger.error("Unhandled exception: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseMessage.error("Unexpected error: " + ex.getMessage()));
    }
}

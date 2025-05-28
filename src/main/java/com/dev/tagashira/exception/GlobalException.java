package com.dev.tagashira.exception;


import com.dev.tagashira.dto.response.ApiResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalException {
    
    // Handle specific not found exceptions
    @ExceptionHandler(value = {
            ApartmentNotFoundException.class,
            ResidentNotFoundException.class,
            VehicleNotFoundException.class,
            FeeNotFoundException.class,
            InvoiceNotFoundException.class,
            UtilityBillNotFoundException.class,
            EntityNotFoundException.class
    })
    public ResponseEntity<ApiResponse<Object>> handleNotFoundException(Exception ex) {
        ApiResponse<Object> res = new ApiResponse<Object>();
        res.setCode(HttpStatus.NOT_FOUND.value()); // 404
        res.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
    }
    
    // Handle data validation exceptions
    @ExceptionHandler(value = {
            InvalidDataException.class
    })
    public ResponseEntity<ApiResponse<Object>> handleInvalidDataException(Exception ex) {
        ApiResponse<Object> res = new ApiResponse<Object>();
        res.setCode(HttpStatus.BAD_REQUEST.value()); // 400
        res.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }
    
    // Handle file processing exceptions
    @ExceptionHandler(value = {
            FileProcessingException.class
    })
    public ResponseEntity<ApiResponse<Object>> handleFileProcessingException(Exception ex) {
        ApiResponse<Object> res = new ApiResponse<Object>();
        res.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value()); // 500
        res.setMessage("File processing error: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
    }
    
    // Handle user info exceptions
    @ExceptionHandler(value = {
            UserInfoException.class,
            UsernameNotFoundException.class
    })
    public ResponseEntity<ApiResponse<Object>> handleUserException(Exception ex) {
        ApiResponse<Object> res = new ApiResponse<Object>();
        res.setCode(HttpStatus.BAD_REQUEST.value()); //400
        res.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }
    
    // Handle generic runtime exceptions (fallback)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException ex) {
        ApiResponse<Object> res = new ApiResponse<Object>();
        res.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value()); //500
        res.setMessage("Internal server error: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentialsException(BadCredentialsException ex) {
        ApiResponse<Object> res = new ApiResponse<Object>();
        res.setCode(HttpStatus.UNAUTHORIZED.value());  // 401 Unauthorized
        res.setMessage("Invalid username or password");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse<Map<String, String>> res = new ApiResponse<>();
        res.setCode(HttpStatus.BAD_REQUEST.value());
        res.setMessage("Validation failed");
        res.setData(errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    @ExceptionHandler(TokenResponseException.class)
    public ResponseEntity<ApiResponse<Object>> handleTokenResponseException(TokenResponseException ex){
        ApiResponse<Object> res = new ApiResponse<Object>();
        res.setCode(HttpStatus.BAD_REQUEST.value());  // 400 Bad request
        res.setMessage("Invalid grant");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }
}

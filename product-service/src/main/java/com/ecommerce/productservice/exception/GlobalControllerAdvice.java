package com.ecommerce.productservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalControllerAdvice {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFoundException(ResourceNotFoundException exception, WebRequest request) {
        log.warn("Recurso no encontrado - Path: {}, Message: {}",
                request.getDescription(false), exception.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());

        problemDetail.setTitle("Recurso no encontrado");
        problemDetail.setType(URI.create("https://api.ecommerce.com/erros/not-found"));
        problemDetail.setProperty("Timestamp", Instant.now());
        problemDetail.setProperty("Resource", exception.getMessage());
        problemDetail.setProperty("Field", exception.getFieldName());
        problemDetail.setProperty("Value", exception.getFieldValue());

        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValidException(MethodArgumentNotValidException exception, WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "La validacion fallo en uno o mas campos");

        problemDetail.setTitle("Error de validación ");
        problemDetail.setType(URI.create("https://api.ecommerce.com/erros/error-validation"));
        problemDetail.setProperty("Timestamp", Instant.now());

        Map<String,String> errorMap = new HashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error -> {
            errorMap.put(error.getField(), error.getDefaultMessage());
        });

        problemDetail.setProperty("error", errorMap);

        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception exception, WebRequest request) {
        log.error("A ocurrido un error inesperado {}:, {}",
                request.getDescription(false), exception.getMessage(), exception);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "A ocurrido un error inesperado. Por favor, contactar con el administrador.");

        problemDetail.setTitle("Internal Server Error");
        problemDetail.setType(URI.create("https://api.ecommerce.com/erros/internal"));
        problemDetail.setProperty("Timestamp", Instant.now());

        return problemDetail;
    }
}

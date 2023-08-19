package com.hetacz.productmanager.exception;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@ControllerAdvice
public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleAllExceptions(Exception ex, WebRequest request) {
        return getResponseEntity(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorDetails> handleNoSuchElementException(Exception ex, WebRequest request) {
        return getResponseEntity(ex, request, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDetails> handleIllegalArgumentException(Exception ex, WebRequest request) {
        return getResponseEntity(ex, request, HttpStatus.BAD_REQUEST);
    }

//    @Override
//    public ResponseEntity<Object> handleMethodArgumentNotValid(
//            @NotNull MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, @NotNull WebRequest request
//    ) {
//        List<String> messages =
//                ex.getFieldErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList();
//
//        ErrorDetails errorDetails = new ErrorDetails(
//                LocalDateTime.now(),
//                messages.toString(),
//                request.getDescription(false)
//        );
//        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
//    }

    @Contract("_, _, _ -> new")
    private @NotNull ResponseEntity<ErrorDetails> getResponseEntity(@NotNull Exception ex, @NotNull WebRequest request, HttpStatus status) {
        return new ResponseEntity<>(new ErrorDetails(
                LocalDateTime.now(),
                ex.getMessage(),
                request.getDescription(false)
        ), status);
    }
}

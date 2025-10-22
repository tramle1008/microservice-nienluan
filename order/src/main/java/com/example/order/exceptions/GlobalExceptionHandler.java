package com.example.order.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice

public class GlobalExceptionHandler {

//    @ExceptionHandler(Exception.class)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity <Map<String, String>> methodArgumentNotValidException(MethodArgumentNotValidException e){
        Map<String, String> respon = new HashMap<>();
        //lấy danh sách lỗi
        e.getBindingResult().getAllErrors().forEach( err -> {
            String fieldname = ((FieldError)err).getField();
            String message = err.getDefaultMessage();
            respon.put(fieldname,message);
        });
        return new ResponseEntity<Map<String,String>>(
                respon, HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> myResourceNotFound(ResourceNotFoundException e){
        String messString = e.getMessage();
        return  new ResponseEntity<>(messString, HttpStatus.NOT_FOUND);

    }
    @ExceptionHandler(APIException.class)
    public ResponseEntity<String> handleAPIException(APIException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<String> handleUnauthorizedException(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

}

package com.example.shoppingcart.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = {InvalidInputException.class})
    public ResponseEntity<CustomResponse> handleInvalidInputException(InvalidInputException e){
       CustomResponse errorResponse = new CustomResponse(
                HttpStatus.BAD_REQUEST,
                e.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {UnauthorizedException.class})
    public ResponseEntity<CustomResponse> handleUnauthorizedException(UnauthorizedException e){
        CustomResponse errorResponse = new CustomResponse(
                HttpStatus.UNAUTHORIZED,
                e.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = {UserNotFoundException.class})
    public ResponseEntity<CustomResponse> handleUserNotFoundException(UserNotFoundException e){
        CustomResponse errorResponse = new CustomResponse(
                HttpStatus.NOT_FOUND,
                e.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {ResourcetNotFoundException.class})
    public ResponseEntity<CustomResponse> handleProductNotFoundException(ResourcetNotFoundException e){
        CustomResponse errorResponse = new CustomResponse(
                HttpStatus.NOT_FOUND,
                e.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }


}

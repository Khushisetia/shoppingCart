package com.example.shoppingcart.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomResponse {

    private int status;
    private String message;

    public CustomResponse(HttpStatus status, String message) {
        this.status = status.value();
        this.message = message;
    }

}

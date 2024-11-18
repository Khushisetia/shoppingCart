package com.example.shoppingcart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Review {
    private String id;
    private String productId;
    private String userId;
    private String comment;
    private int rating;

    private Date date;
}

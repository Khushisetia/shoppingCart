package com.example.shoppingcart.dto.user;

import lombok.Data;

@Data
public class OrderItemRequest {

    private String productId;
    private String productName;
    private int quantity;
    private double price;

}

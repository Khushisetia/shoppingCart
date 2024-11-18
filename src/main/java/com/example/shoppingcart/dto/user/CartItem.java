package com.example.shoppingcart.dto.user;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data

@NoArgsConstructor

public class CartItem {

    private String productId;
    private String productName;
    private double price;
    private int quantity;

    public CartItem(String productId, String productName, double price, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        return price * quantity;
    }



    // Getters and Setters
}


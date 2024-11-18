package com.example.shoppingcart.dto;

public enum Role {
    USER,
    ADMIN,
    SELLER;

    @Override
    public String toString() {
        return "ROLE_" + name();
    }
}

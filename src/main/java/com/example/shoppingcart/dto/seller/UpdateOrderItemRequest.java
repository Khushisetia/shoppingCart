package com.example.shoppingcart.dto.seller;

import com.example.shoppingcart.dto.OrderStatus;
import lombok.Data;

@Data
public class UpdateOrderItemRequest {
    private String productId;  // The ID of the product
    private OrderStatus orderStatus;  // Status for this specific item
    private int quantity;  // Quantity for this specific item (if needed)

    // Add other fields you want to update, like price, color, etc.
}
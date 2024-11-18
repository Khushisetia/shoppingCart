package com.example.shoppingcart.models;

import com.example.shoppingcart.dto.user.CartItem;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Document(collection = "carts") // This annotation indicates that this class is a MongoDB document
public class Cart {
    @Id
    private String id; // Optional: if you want a unique identifier for the cart
    private String userId; // User ID for the cart owner
    private List<CartItem> cartItems = new ArrayList<>(); // List of items in the cart

    public Cart(String userId) {
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void addCartItem(CartItem cartItem) {
        for (CartItem item : cartItems) {
            if (item.getProductId().equals(cartItem.getProductId())) {
                item.setQuantity(item.getQuantity() + cartItem.getQuantity());
                return; // Item already exists, so update quantity
            }
        }
        cartItems.add(cartItem); // Add new item if it doesn't exist
    }

    public void removeCartItem(String productId) {
        cartItems.removeIf(item -> item.getProductId().equals(productId)); // Remove item by product ID
    }

    public double getTotal() {
        return cartItems.stream().mapToDouble(CartItem::getTotalPrice).sum(); // Calculate total price
    }


}

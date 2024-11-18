package com.example.shoppingcart.service;

import com.example.shoppingcart.dto.user.CartItem;
import com.example.shoppingcart.models.Cart;
import com.example.shoppingcart.models.Product;
import com.example.shoppingcart.repo.CartRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private ProductService productService;

    @Autowired
    private CartRepo cartRepo;


    public boolean checkIfItemExistsInCart(String userId, String productId) {
        // Retrieve the cart for the specified user
        Optional<Cart> cartOptional = cartRepo.findByUserId(userId);

        if (cartOptional.isPresent()) {
            Cart cart = cartOptional.get();
           
            return cart.getCartItems().stream()
                    .anyMatch(cartItem -> cartItem.getProductId().equals(productId));
        }

        // If cart doesn't exist for user, return false
        return false;
    }

    public boolean addItemToCart(String userId, CartItem cartItem) {
        // Retrieving the product details
        Optional<Product> productOptional = productService.findProductById(cartItem.getProductId());
        if (!productOptional.isPresent()) {
            throw new IllegalArgumentException("Product not found.");
        }

        Product product = productOptional.get();

        // Checking if the requested quantity exceeds available stock
        if (cartItem.getQuantity() > product.getQuantity()) {
            throw new IllegalArgumentException("Requested quantity exceeds available stock.");
        }

        // Checking if item already exists in the cart
        Cart cart = cartRepo.findByUserId(userId).orElseGet(() -> new Cart(userId));
        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProductId().equals(cartItem.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            // Updating quantity and price if item is already in cart
            int newQuantity = existingItem.get().getQuantity() + cartItem.getQuantity();

            // Checking item's quantity should not be more than stock
            if (newQuantity > product.getQuantity()) {
                throw new IllegalArgumentException("Total requested quantity exceeds available stock.");
            }

            existingItem.get().setQuantity(newQuantity);
        } else {
            // Add new item to the cart
            cart.getCartItems().add(cartItem);
        }

        cartRepo.save(cart);
        return true;
    }



    public boolean removeItemFromCart(String userId, String productId) {
        // Finding the cart for the user
        Cart cart = cartRepo.findByUserId(userId).orElse(null);
        if (cart == null) {
            return false; // No cart found
        }

        // Finding the CartItem to remove
        CartItem itemToRemove = cart.getCartItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElse(null);

        if (itemToRemove != null) {
            // Removing the item from the cart
            cart.getCartItems().remove(itemToRemove);
            cartRepo.save(cart); // Saving the updated cart
            return true;
        } else {
            return false; // Item not found
        }
    }


    public Cart getCartByUserId(String userId) {
        return cartRepo.findByUserId(userId).orElse(null); // Retrieving the cart for the user
    }


    public double getCartTotal(String userId) {
        Cart cart = getCartByUserId(userId);
        return cart != null ? cart.getTotal() : 0.0; // Return total price of the cart
    }


    public void updateCartItemQuantity(String userId, String productId, int quantity) {
        // Fetching the cart for the user
        Cart cart = cartRepo.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("Cart not found"));

        // Finding the cart item
        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Product not found in cart"));

        // Fetching product to check stock availability
        Product product = productService.findProductById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // Checking if requested quantity exceeds available stock
        if (quantity > product.getQuantity()) {
            throw new IllegalArgumentException("Requested quantity exceeds available stock");
        }

        // Updaing the quantity
        cartItem.setQuantity(quantity);
        cartRepo.save(cart);
    }

}

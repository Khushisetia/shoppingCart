package com.example.shoppingcart.controllers;

import com.example.shoppingcart.dto.user.CartItem;
import com.example.shoppingcart.dto.user.UpdateQuantityRequest;
import com.example.shoppingcart.models.Cart;
import com.example.shoppingcart.models.Product;
import com.example.shoppingcart.models.User;
import com.example.shoppingcart.repo.CartRepo;
import com.example.shoppingcart.repo.UserRepo;
import com.example.shoppingcart.service.CartService;
import com.example.shoppingcart.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user/cart")
@CrossOrigin(origins = "http://127.0.0.1:5500", allowCredentials = "true")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepo userRepo;


    @Autowired
    private CartRepo cartRepo;

    @Autowired
    private ProductService productService;

    @PostMapping("/add")
    public ResponseEntity<String> addToCart(@RequestBody CartItem cartItem) {
        // Retrieve authentication information
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // Assuming the username is the userId

        // Fetch userId from the repository based on the username
        Optional<User> userOptional = userRepo.findByUsername(username);
        if (!userOptional.isPresent()) {
            return ResponseEntity.badRequest().body("User not found.");
        }

        String userId = userOptional.get().get_id(); // Get userId from User entity
        Optional<Product> productOpt = productService.findProductById(cartItem.getProductId());
        if (!productOpt.isPresent()) {
            return ResponseEntity.badRequest().body("Product not found.");
        }

        Product product = productOpt.get();
        if (product.getQuantity() < cartItem.getQuantity()) {
            return ResponseEntity.badRequest().body("Insufficient stock for " + product.getName());
        }


        if (cartItem.getPrice() != product.getPrice()) {
            return ResponseEntity.badRequest().body("Price mismatch. Please update your cart.");
        }



        // Add item to cart
        cartService.addItemToCart(userId, cartItem);
        return ResponseEntity.ok("Item added to cart successfully");
    }

    @DeleteMapping("/remove")
    public ResponseEntity<String> removeFromCart(@RequestParam String productId) {
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        String userName=authentication.getName();

        Optional<User> user=userRepo.findByUsername(userName);
        String userId=user.get().get_id();
        boolean itemExists = cartService.checkIfItemExistsInCart(userId, productId);
        if (!itemExists) {
            return ResponseEntity.status(404).body("Item not found in cart.");
        }

        boolean removed = cartService.removeItemFromCart(userId, productId);
        if (removed) {
            return ResponseEntity.ok("Item removed from cart successfully");
        } else {
            return ResponseEntity.status(404).body("Failed to remove item from cart");
        }
    }


    @GetMapping("/total")
    public ResponseEntity<Double> getCartTotal(@RequestParam String userId) {
        double total = cartService.getCartTotal(userId);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/viewCartItems")
    public ResponseEntity<List<CartItem>> viewCartItems() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Fetch userId from the repository based on the username
        Optional<User> userOptional = userRepo.findByUsername(username);
        if (!userOptional.isPresent()) {
            return ResponseEntity.badRequest().body(Collections.emptyList()); // Handle user not found
        }

        String userId = userOptional.get().get_id();

        // Retrieve the cart for the user
        Cart cart = cartRepo.findByUserId(userId).orElse(null);
        if (cart == null || cart.getCartItems().isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList()); // Return an empty list if no items found
        }

        // Create a list of CartItem with product details, quantity, and price
        List<CartItem> cartDetails = cart.getCartItems().stream()
                .map(cartItem -> {
                    Optional<Product> productOpt = productService.findProductById(cartItem.getProductId());
                    if (productOpt.isPresent()) {
                        Product product = productOpt.get();
                        return new CartItem(product.getId(), product.getName(), cartItem.getPrice(), cartItem.getQuantity());
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull) // Filter out any null responses
                .collect(Collectors.toList());



        return ResponseEntity.ok(cartDetails);
    }

    @PutMapping("/updateQuantity")
    public ResponseEntity<?> updateCartQuantity(@RequestBody UpdateQuantityRequest request) throws Exception {
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        String userName=authentication.getName();
        Optional<User> user=userRepo.findByUsername(userName);
        if(!user.isPresent()) throw new Exception("User not found");

        String userId=user.get().get_id();

        try {
            cartService.updateCartItemQuantity(userId, request.getProductId(), request.getQuantity());
            return ResponseEntity.ok(Map.of("message", "Quantity updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }



}

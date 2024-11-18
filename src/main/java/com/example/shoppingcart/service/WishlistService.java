package com.example.shoppingcart.service;

import com.example.shoppingcart.dto.user.WishlistItem;
import com.example.shoppingcart.models.Wishlist;
import com.example.shoppingcart.repo.ProductRepo;
import com.example.shoppingcart.repo.WishlistRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
public class WishlistService {
    @Autowired
    private WishlistRepo wishlistRepo;

    @Autowired
    private ProductRepo productRepo; // Inject the product repository

    public List<WishlistItem> viewWishlist(String userId) {
        Wishlist wishlist = (Wishlist) wishlistRepo.findByUserId(userId).orElse(null);
        if (wishlist != null) {
            return new ArrayList<>(wishlist.getProduct()); // Return the list of WishlistItem
        }
        return List.of(); // Return an empty list if no wishlist found
    }

    // Add a product to the wishlist
    // Add a product to the wishlist
    public String addProductToWishlist(String userId, WishlistItem wishlistItem) {

        if (wishlistItem == null || userId == null) {
            throw new IllegalArgumentException("UserId or WishlistItem cannot be null");
        }
        // Set the userId in the wishlistItem
        Wishlist wishlist = wishlistRepo.findByUserId(userId).orElse(new Wishlist());

        // If the wishlist does not exist, create a new one
        if (wishlist.getProduct() == null) {
            wishlist.setProduct(new HashSet<>()); // Initialize the products list if it's null
        }

        // Check if the product already exists in the wishlist
        boolean exists = wishlist.getProduct().stream()
                .anyMatch(item -> item.getProductId().equals(wishlistItem.getProductId()));

        if (!exists) {
            wishlist.getProduct().add(wishlistItem); // Add WishlistItem to the wishlist
            wishlist.setUserId(userId); // Set userId in the Wishlist only
            wishlistRepo.save(wishlist); // Save the updated wishlist back to the database
            return "Product added successfully in wishlist.";
        } else {
            return "Product is already in the wishlist.";
        }
    }


    // Remove a product from the wishlist
    public void removeProductFromWishlist(String userId, String productId) {
        Wishlist wishlist = (Wishlist) wishlistRepo.findByUserId(userId).orElse(null);
        if (wishlist != null) {
            wishlist.getProduct().removeIf(item -> item.getProductId().equals(productId)); // Remove the WishlistItem
            wishlistRepo.save(wishlist); // Save the updated wishlist
        }
    }
}
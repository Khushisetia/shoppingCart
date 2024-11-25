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
    private ProductRepo productRepo; 

    public List<WishlistItem> viewWishlist(String userId) {
        Wishlist wishlist = (Wishlist) wishlistRepo.findByUserId(userId).orElse(null);
        if (wishlist != null) {
            return new ArrayList<>(wishlist.getProduct()); 
        }
        return List.of(); // Return an empty list if no wishlist found
    }

    
    // Add a product to the wishlist
    public String addProductToWishlist(String userId, WishlistItem wishlistItem) {

        if (wishlistItem == null || userId == null) {
            throw new IllegalArgumentException("UserId or WishlistItem cannot be null");
        }
       
        Wishlist wishlist = wishlistRepo.findByUserId(userId).orElse(new Wishlist());

        
        if (wishlist.getProduct() == null) {
            wishlist.setProduct(new HashSet<>()); 
        }

        // Check if the product already exists in the wishlist
        boolean exists = wishlist.getProduct().stream()
                .anyMatch(item -> item.getProductId().equals(wishlistItem.getProductId()));

        if (!exists) {
            wishlist.getProduct().add(wishlistItem); 
            wishlist.setUserId(userId); 
            wishlistRepo.save(wishlist); 
            return "Product added successfully in wishlist.";
        } else {
            return "Product is already in the wishlist.";
        }
    }


    // Remove a product from the wishlist
    public void removeProductFromWishlist(String userId, String productId) {
        Wishlist wishlist = (Wishlist) wishlistRepo.findByUserId(userId).orElse(null);
        if (wishlist != null) {
            wishlist.getProduct().removeIf(item -> item.getProductId().equals(productId)); 
            wishlistRepo.save(wishlist); // Save the updated wishlist
        }
    }
}

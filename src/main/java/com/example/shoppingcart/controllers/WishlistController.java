package com.example.shoppingcart.controllers;

import com.example.shoppingcart.dto.user.WishlistItem;
import com.example.shoppingcart.models.Product;
import com.example.shoppingcart.models.User;
import com.example.shoppingcart.repo.ProductRepo;
import com.example.shoppingcart.repo.UserRepo;
import com.example.shoppingcart.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    UserRepo userRepo;

    @Autowired
    ProductRepo productRepo;

    // View wishlist items for a specific user
    @GetMapping("/view")
    public ResponseEntity<List<WishlistItem>> viewWishlist() {
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        String username=authentication.getName();
        Optional<User> user= userRepo.findByUsername(username);
        if(!user.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        String userId=user.get().get_id();
            List<WishlistItem> wishlistItems = wishlistService.viewWishlist(userId);
            return ResponseEntity.ok(wishlistItems); // Return the wishlist items}
    }

    // Add a product to the user's wishlist
//    @PostMapping("/addProduct")
//    public ResponseEntity<String> addProductToWishlist( @RequestBody WishlistItem wishlistItem) {
//        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
//        String username=authentication.getName();
//        Optional<User> user= userRepo.findByUsername(username);
//        if(!user.isPresent()) {
//            return ResponseEntity.notFound().build();
//        }
//        String userId=user.get().get_id();
//        wishlistService.addProductToWishlist(userId, wishlistItem);
//        return ResponseEntity.ok("Product added to wishlist successfully.");
//    }

    @PostMapping("/addProduct")
    public ResponseEntity<String> addProductToWishlist(@RequestParam String productId) {
        // Get the authenticated username
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Fetch the user from the repository
        Optional<User> user = userRepo.findByUsername(username);
        if (!user.isPresent()) {
            return ResponseEntity.notFound().build(); // User not found
        }

        // Fetch product by ID
        Optional<Product> product = productRepo.findById(productId);
        if (!product.isPresent()) {
            return ResponseEntity.notFound().build(); // Product not found
        }

        // Create WishlistItem (You may need to add more attributes depending on your WishlistItem class)
        WishlistItem wishlistItem = new WishlistItem();
        // wishlistItem.setUserId(user.get().get_id());
        wishlistItem.setProductId(productId);
        wishlistItem.setProductName(product.get().getName());// Set product details (assuming WishlistItem has a Product field)
        wishlistItem.setPrice(product.get().getPrice());

        wishlistService.addProductToWishlist(user.get().get_id(), wishlistItem);
        // Add the item to the wishlist
        //return ResponseEntity.ok(wishlistService.addProductToWishlist(user.get().get_id(), wishlistItem));

        return ResponseEntity.ok("Product added to wishlist successfully.");
    }

    // Remove a product from the user's wishlist
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<String> removeProductFromWishlist(@PathVariable String productId) {
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        String username=authentication.getName();
        Optional<User> user= userRepo.findByUsername(username);
        if(!user.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        String userId=user.get().get_id();
        wishlistService.removeProductFromWishlist(userId, productId);
        return ResponseEntity.ok("Product removed from wishlist successfully.");
    }
}




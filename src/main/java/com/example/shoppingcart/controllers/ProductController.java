package com.example.shoppingcart.controllers;

import com.example.shoppingcart.models.Product;
import com.example.shoppingcart.models.User;
import com.example.shoppingcart.repo.ReviewRepo;
import com.example.shoppingcart.repo.UserRepo;
import com.example.shoppingcart.service.OrderService;
import com.example.shoppingcart.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user/seller")
public class ProductController {

    @Autowired
    ProductService productService;

    @Autowired
    UserRepo userRepo;

    @Autowired
    ReviewRepo reviewRepo;

    @Autowired
    OrderService orderService;
    // Add ObjectMapper to convert JSON to Product object
    private final ObjectMapper objectMapper = new ObjectMapper();



    private boolean isSeller() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> user = userRepo.findByUsername(username);
        return user.isPresent() && Boolean.TRUE.equals(user.get().isBeASeller());
    }


        @PostMapping("/Addproduct")
        public ResponseEntity<Product> addProduct(
                @RequestParam("file") MultipartFile file,
                @RequestParam("product") String productJson) {
            if (!isSeller()) {
                return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
            }

            try {
                Product product = objectMapper.readValue(productJson, Product.class);
                Product savedProduct = productService.addProduct(product, file);
                return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
            } catch (Exception e) {
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }
        }

//    @PostMapping("/AddProduct")
//    public ResponseEntity<Product> addProduct(
//            @RequestParam("files") List<MultipartFile> files, // Accept multiple files
//            @RequestParam("product") String productJson) {
//        if (!isSeller()) {
//            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
//        }
//
//        try {
//            // Parse the product JSON to a Product object
//            Product product = objectMapper.readValue(productJson, Product.class);
//
//            // Add the product with multiple images
//            Product savedProduct = productService.addProduct(product, files);
//
//            return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
//        } catch (Exception e) {
//            e.printStackTrace(); // Log the error for debugging
//            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
//        }
//    }





    @GetMapping("/search")
    public List<Product> searchByProductName(@RequestParam String productName){
        return productService.searchByProductName(productName);

    }

    @GetMapping("/searchBySeller")
    public List<Product> searchBySellerName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName(); // Assuming the user ID is the username
        Optional<User> user = userRepo.findByUsername(userName);

        if (user.isPresent()) {
            String sellerId = user.get().get_id();
            return productService.searchBySellerId(sellerId);
        } else {
            // Handle the case where the user is not found (return an empty list or throw an exception)
            return Collections.emptyList();
        }
    }



    @PutMapping("/updateProduct/{id}")
    public ResponseEntity<String> updatingProduct(@PathVariable String id,@RequestBody Product product){
        if (!isSeller()) {
            return new ResponseEntity<>("Only sellers can update products", HttpStatus.FORBIDDEN);
        }

        String response = productService.updationOfProducts(id, product);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/deleteProduct/{productId}")
    public ResponseEntity<String> deleteProduct(@PathVariable String productId) {
        if (!isSeller()) {
            return new ResponseEntity<>("Only sellers can delete products", HttpStatus.FORBIDDEN);
        }

        String response = productService.deleteProductById(productId);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/viewProducts")
    public ResponseEntity<List<Product>> viewProducts(){
            if (!isSeller()) {
                return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
            }

            // Fetch the seller ID (same as user ID) from the authentication token
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();// Assuming the user ID is the username
            Optional<User> user=userRepo.findByUsername(userName);
            String sellerId=user.get().get_id();
            return productService.viewProducts(sellerId);
        }

//    @GetMapping("/user/seller/viewAllOrders")
//    public List<Order> viewAllOrders() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String userName = authentication.getName();
//        Optional<User> user = userRepo.findByUsername(userName);
//
//        if (user.isPresent()) {
//            String userId = user.get().get_id();
//
//            // Fetch all products associated with this seller
//            List<Product> sellerProducts = productService.findProductsBySellerId(userId);
//            List<String> sellerProductIds = sellerProducts.stream()
//                    .map(Product::getId)
//                    .collect(Collectors.toList());
//
//            // Fetch orders containing any of the seller's product IDs
//            return orderService.findOrdersByProductIds(sellerProductIds);
//        } else {
//            // Handle case where user is not found (return an empty list or throw an exception)
//            return Collections.emptyList();
//        }
//    }
//
//
//        }





}


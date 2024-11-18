package com.example.shoppingcart.controllers;

import com.example.shoppingcart.dto.user.OrderRequest;
import com.example.shoppingcart.dto.Review;
import com.example.shoppingcart.dto.admin.ReviewRequest;
import com.example.shoppingcart.exceptions.UnauthorizedException;
import com.example.shoppingcart.exceptions.UserNotFoundException;
import com.example.shoppingcart.models.Complaints;
import com.example.shoppingcart.models.Order;
import com.example.shoppingcart.models.Product;
import com.example.shoppingcart.models.User;
import com.example.shoppingcart.repo.ProductRepo;
import com.example.shoppingcart.repo.ReviewRepo;
import com.example.shoppingcart.repo.UserRepo;
import com.example.shoppingcart.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController

public class UserController {

    @Autowired
    private MyUserService userService;

    @Autowired
    UserRepo userRepo;

    @Autowired
    OrderService orderService;

    @Autowired
    ProductService productService;

    @Autowired
    ProductRepo productRepo;

    @Autowired
    ReviewRepo reviewRepo;

    @Autowired
    ComplaintService complaintService;




    @PostMapping("/register")
    public ResponseEntity<String>  userRegistration(@RequestBody User user){
        try{
            userService.addUser(user);
            return new ResponseEntity<>("Success", HttpStatus.OK);
        }
        catch (IllegalArgumentException iae){
            return new ResponseEntity<>(iae.getMessage(),HttpStatus.BAD_REQUEST);
        }

    }


    @GetMapping("/user/Hello")
    public String checking(){
        return "Hello world";
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        String token = userService.verify(user);
        if ("Fail".equals(token)) {
            throw new UnauthorizedException("Login failed. Please check your credentials.");
        }
        User authenticatedUser = userRepo.findByUsername(user.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return ResponseEntity.ok(Map.of("token", token, "role", authenticatedUser.getRole().toString(), "beASeller", authenticatedUser.isBeASeller()));
    }




//    @PutMapping("/user/update")
//    public ResponseEntity<String> updateUser(@RequestBody User updatedUser, Principal principal) {
//        String username = principal.getName();
//        Optional<User> userOptional = userRepo.findByUsername(username);
//        if (userOptional.isPresent()) {
//            User user = userOptional.get();
//            user.setEmail(updatedUser.getEmail());
//            user.setPhoneNumber(updatedUser.getPhoneNumber());
//            user.setFullName(updatedUser.getFullName());
//            user.setAddress(updatedUser.getAddress());
//            System.out.println("user "+user);
//            userRepo.save(user);
//            return ResponseEntity.ok("User profile updated successfully");
//        }
//        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
//    }

    @PutMapping("/user/update")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<String> updateUser(@RequestBody User updatedUser, Principal principal) {
        // Get the username of the currently authenticated user
        String username = principal.getName();

        // Fetch the current user from the database using their username
        Optional<User> userOptional = userRepo.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Update fields from updatedUser object
            user.setEmail(updatedUser.getEmail());
            user.setPhoneNumber(updatedUser.getPhoneNumber());
            user.setFullName(updatedUser.getFullName());
            user.setAddress(updatedUser.getAddress());

            // Log the details before saving
            System.out.println("Updated user details: " + user);

            // Save the updated user back to the repository
            userRepo.save(user);
            return ResponseEntity.ok("User profile updated successfully");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }


    @PostMapping("user/placeOrder")
     public ResponseEntity<Order> placeOrder(@RequestBody OrderRequest order,
                                              @RequestHeader("Authorization") String token) {
                // Remove "Bearer " prefix if present
                if (token.startsWith("Bearer ")) {
                    token = token.substring(7);
                }

                try {
                    // Call the service layer to place the order
                    Order newOrder = orderService.placeOrder(order, token);
                    return ResponseEntity.ok(newOrder);
                } catch (Exception e) {
                    // Log the exception for debugging
                    e.printStackTrace();  // Or use a logging framework like Log4j or SLF4J
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                }
            }


    @PostMapping("user/cancel/{orderId}")
    public ResponseEntity<String> cancelOrder(
            @PathVariable String orderId,
            @RequestParam String productId,
            @RequestParam String cancellationReason) {
        try {
            // Call the service method to cancel the specific item
            orderService.cancelOrder(orderId, productId, cancellationReason);

            return ResponseEntity.ok("Item with Product ID " + productId + " in Order " + orderId + " has been successfully cancelled.");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Failed to cancel item: " + e.getMessage());
        }
    }




    @DeleteMapping("user/deletingAccount")
    public ResponseEntity<String> deleteUserAccount(Principal principal){
        String userName= principal.getName();
        boolean isDeleted = userService.deleteUserByUsername(userName);

        if (isDeleted) {
            return ResponseEntity.status(HttpStatus.OK).body("User account deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User account not found.");
        }
    }

    @GetMapping("/user/order/{orderId}")
    public Order getOrder(@PathVariable String orderId) throws Exception {
        return orderService.viewOrder(orderId);
    }
    @GetMapping("/user/viewAllOrders")
    public ResponseEntity<List<Order>> getAllOrder() throws Exception {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();

            // Retrieve user by username
            Optional<User> user = userRepo.findByUsername(userName);
            if (!user.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
            }

            String userId = user.get().get_id();

            // Get all orders for the user
            List<Order> orders = orderService.viewAllOrder(userId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            // Handle any exceptions that might occur
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }


    @GetMapping("user/search")
    public List<Product> searchByProductName(@RequestParam String productName){
        return productService.searchByProductName(productName);

    }

    @GetMapping("user/searchBySeller")
    public List<Product> searchBySellerName(@RequestParam String sellerName){
        return  productService.searchBySellerName(sellerName);
    }

    @GetMapping("user/{productId}")
    public Optional<Product> searchBSProductId(@PathVariable String productId){
        return  productService.searchById(productId);
    }


    @GetMapping("/user/viewAllProducts")
    public ResponseEntity<List<Product>> viewAllProducts(){
        List<Product> products=productRepo.findAll();
        return ResponseEntity.ok(products);
    }




    @GetMapping("user/category")
    public ResponseEntity<List<Product>> searchByCategory(@RequestParam String category) {
        List<Product> products = productService.findProductsByCategory(category);
        if (products.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(products);
    }

//    @GetMapping("/downloadInvoice/{orderId}")
//    public ResponseEntity<Resource> downloadInvoice(@PathVariable String orderId) {
//        String filePath = "invoices/order_" + orderId + ".pdf";
//        FileSystemResource fileResource = new FileSystemResource(filePath);
//
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice_" + orderId + ".pdf")
//                .contentType(MediaType.APPLICATION_PDF)
//                .body(fileResource);
//    }


//    add reviews
     @PutMapping("user/review")
          public ResponseEntity<String> addReview(
                 @RequestBody ReviewRequest reviewRequest) {
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        String userName=authentication.getName();
        Optional<User> user=userRepo.findByUsername(userName);
        String userId=user.get().get_id();
    try {
        String result = orderService.addReview(userId, reviewRequest);
        return ResponseEntity.ok(result);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}


//Register Complaints
        @PostMapping("user/registerComplaints")
        public ResponseEntity<String> registerComplaint(@RequestBody Complaints complaints){
            Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
            String userName=authentication.getName();
            Optional<User> user = userRepo.findByUsername(userName);
            if (user.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
            String userId=user.get().get_id();
            complaintService.register(userId,complaints.getDescription());
            return ResponseEntity.ok("Complaint Registered Successfully");
        }



    @GetMapping("user/getComplaints")
    public ResponseEntity<List<Complaints>> getAllComplaintsOfUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();

        Optional<User> user = userRepo.findByUsername(userName);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        String userId = user.get().get_id();
        List<Complaints> complaints = complaintService.getUserComplaints(userId); // assuming this returns a List
        return ResponseEntity.ok(complaints);
    }

    @GetMapping("user/details")
    public ResponseEntity<User> getDetails(){
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        String userName=authentication.getName();
        Optional<User> user=userRepo.findByUsername(userName);
        if(!user.isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(user.get());
    }


    @GetMapping("user/getReviewByProductId/{productId}")
    public List<Review> getReviews(@PathVariable String productId){
        return reviewRepo.findByProductId(productId);
    }




}


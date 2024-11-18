package com.example.shoppingcart.controllers;

import com.example.shoppingcart.dto.*;
import com.example.shoppingcart.dto.seller.Seller_RequestDTO;
import com.example.shoppingcart.dto.seller.Seller_login;
import com.example.shoppingcart.dto.seller.UpdateOrderItemRequest;
import com.example.shoppingcart.dto.seller.UpdateOrderRequest;
import com.example.shoppingcart.dto.user.OrderItem;
import com.example.shoppingcart.dto.user.OrderRequest;
import com.example.shoppingcart.models.Order;
import com.example.shoppingcart.models.User;
import com.example.shoppingcart.repo.OrderRepo;
import com.example.shoppingcart.repo.Seller_RequestRepo;
import com.example.shoppingcart.repo.UserRepo;
import com.example.shoppingcart.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
public class SellerController {

    @Autowired
    SellerService sellerService;

    @Autowired
    UserRepo userRepo;

    @Autowired
    JWTService jwtService;

    @Autowired
    OrderService orderService;

    @Autowired
    Seller_RequestRepo sellerRequestRepo;

    @Autowired
    OrderRepo orderRepo;

    @Autowired
    ProductService productService;


    @Autowired
    private EmailService emailService;


    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("user/registerAsASeller")
    public ResponseEntity<?> registerAsASeller(@RequestBody Seller_RequestDTO sellerRequestDTO, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = authHeader;

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // Remove "Bearer " prefix
        }

        String username = jwtService.extractUserName(token);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token: Unable to extract username.");
        }
        if (!isValidEmail(sellerRequestDTO.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid email format.");
        }

        // Find the user by username
        Optional<User> userOptional = userRepo.findByUsername(username);
        if (!userOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }

        User user = userOptional.get();
        String userId=user.get_id();

        // Checking if there is already a pending request
        Optional<OrderRequest.Seller_Request> requestOptional = sellerRequestRepo.findByUsername(username);
        if (requestOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("There is already a pending seller request.");
        }
        
       
        // Creating a new seller request
        OrderRequest.Seller_Request sellerRequest = new OrderRequest.Seller_Request();
        sellerRequest.setId(userId);
        sellerRequest.setUsername(username);
        sellerRequest.setPassword(passwordEncoder.encode(sellerRequestDTO.getPassword()));
        sellerRequest.setRequestDate(LocalDateTime.now());
        sellerRequest.setEmail(sellerRequestDTO.getEmail());
        sellerRequest.setGender(sellerRequestDTO.getGender());
        sellerRequest.setPhoneNumber(sellerRequestDTO.getPhoneNumber());
        sellerRequest.setSellerName(sellerRequestDTO.getSellerName());
        sellerRequest.setBusinessName(sellerRequestDTO.getBusinessName());
        sellerRequest.setBusinessDetails(sellerRequestDTO.getBusinessDetails());


        sellerRequest.setApproved(false); // Initially, not approved

        sellerRequestRepo.save(sellerRequest);

        return ResponseEntity.ok("Seller registration request submitted successfully. Please wait for admin approval.");
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }


    @GetMapping("user/seller/orders/{sellerId}")
       public ResponseEntity<List<Order>> getSellerOrders(@PathVariable String sellerId) {
        List<Order> orders = orderService.sellerOrder(sellerId);
        if (orders.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(orders);
        }
        return ResponseEntity.ok(orders);
    }



    @PutMapping("user/seller/updateOrder/{orderId}")
    public ResponseEntity<String> updateOrder(@PathVariable String orderId,
                                              @RequestBody UpdateOrderRequest updateOrderRequest,
                                              @RequestHeader("Authorization") String token) {
        try {
            // First, update the general order details (address, phone number, etc.)
            String response = orderService.updateOrder(orderId, updateOrderRequest, token);

            // Iterate over the items in the update request and update their statuses
            for (UpdateOrderItemRequest itemRequest : updateOrderRequest.getItems()) {
                String productId = itemRequest.getProductId();
                OrderStatus newStatus = itemRequest.getOrderStatus();

                // Update the status for each item in the order
                orderService.updateOrderItemStatus(orderId, productId, newStatus);

                // If the item is marked as DELIVERED, send a review request email
                if (OrderStatus.DELIVERED.equals(newStatus)) {
                    // Fetch the customer's email from the order ID
                    String customerEmail = getCustomerEmailByOrderId(orderId);
                    emailService.sendReviewRequestEmail(customerEmail, productId);
                }
            }

            // Return a success response with the update confirmation
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Handle exceptions and return an appropriate error response
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to update order: " + e.getMessage());
        }
    }


    @GetMapping("/user/seller/getAllOrders")
    public ResponseEntity<List<Order>> getOrdersForSeller() {
        // Get the current authentication object from the security context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        // Retrieve the user by username
        Optional<User> user = userRepo.findByUsername(username);
        if (user.isEmpty()) {
            // Return 404 if the user is not found
            return ResponseEntity.notFound().build();
        }
        // Extract the userId (or sellerId depending on your design)
        String userId = user.get().get_id();
        // Fetch all product IDs associated with the seller (userId corresponds to sellerId)
        List<String> productIds = productService.getProductIdsBySellerId(userId);
        if (productIds.isEmpty()) {
            // Return 404 if no products are found for the seller
            return ResponseEntity.notFound().build();
        }
        // Fetch all orders
            List<Order> orders = orderRepo.findAll();
       
        List<Order> filteredOrders = orders.stream()
                .map(order -> {
                    // Filter items to include only those belonging to the seller's products
                    List<OrderItem> sellerItems = order.getItems().stream()
                            .filter(item -> productIds.contains(item.getProductId()))
                            .collect(Collectors.toList());

                    // Set the filtered items back to the order
                    order.setItems(sellerItems);
                    return order;
                })
                .filter(order -> !order.getItems().isEmpty()) // Only keep orders that have seller items
                .collect(Collectors.toList());

        // Return the filtered orders
        return ResponseEntity.ok(filteredOrders);
    }




    private String getCustomerEmailByOrderId(String orderId) {
        Optional<Order> order=orderRepo.findById(orderId);
        String email=order.get().getEmail();
        return email;
    }

    @PostMapping("user/seller/loginAsASeller")
    public ResponseEntity<String> loginAsASeller(@RequestBody Seller_login sellerLogin){
       String seller=sellerService.loginASASeller(sellerLogin);
       return ResponseEntity.ok(seller);
    }

}

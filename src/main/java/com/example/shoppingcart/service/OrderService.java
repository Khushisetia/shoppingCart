package com.example.shoppingcart.service;

import com.example.shoppingcart.dto.*;
import com.example.shoppingcart.dto.admin.ReviewRequest;
import com.example.shoppingcart.dto.seller.UpdateOrderItemRequest;
import com.example.shoppingcart.dto.seller.UpdateOrderRequest;
import com.example.shoppingcart.dto.user.CustomerDto;
import com.example.shoppingcart.dto.user.OrderItem;
import com.example.shoppingcart.dto.user.OrderItemRequest;
import com.example.shoppingcart.dto.user.OrderRequest;
import com.example.shoppingcart.models.Order;
import com.example.shoppingcart.models.Product;
import com.example.shoppingcart.models.User;
import com.example.shoppingcart.repo.OrderRepo;
import com.example.shoppingcart.repo.ProductRepo;
import com.example.shoppingcart.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SellerService sellerService;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    ProductRepo productRepo;

    @Autowired
    ProductService productService;


    private OrderItem findOrderItemByProductId(Order order, String productId) {
        return order.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElse(null);
    }

    private void updateOrderItemDetails(OrderItem item, UpdateOrderItemRequest itemRequest) {
        if (itemRequest.getOrderStatus() != null) {
            item.setOrderStatus(itemRequest.getOrderStatus());
        }
    }


    public Order placeOrder(OrderRequest orderRequest, String token) throws Exception {
        String userName = jwtService.extractUserName(token);

        User user = userRepo.findByUsername(userName)
                .orElseThrow(() -> new Exception("User not found"));

        Order newOrder = new Order();
        newOrder.setUserId(user.get_id());
        newOrder.setUserName(user.getUsername());
        newOrder.setEmail(user.getEmail());
        newOrder.setPhoneNumber(orderRequest.getPhoneNumber());
        newOrder.setOrderDate(LocalDateTime.now());

        LocalDateTime deliveryDate = newOrder.getOrderDate().plusDays(6 + (int)(Math.random() * 2));
        newOrder.setDeliveryDate(deliveryDate);

        double totalAmount = 0;
        List<OrderItem> orderItemList = new ArrayList<>();

        for (OrderItemRequest itemRequest : Optional.ofNullable(orderRequest.getItems()).orElse(Collections.emptyList())) {

            Product product = productRepo.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new Exception("Product not found: " + itemRequest.getProductId()));

            if (product.getQuantity() < itemRequest.getQuantity()) {
                throw new Exception("Insufficient stock for product: " + itemRequest.getProductId());
            }
            double totalPrice = itemRequest.getQuantity() * itemRequest.getPrice();
            OrderItem item = new OrderItem(
                    itemRequest.getProductId(),
                    itemRequest.getProductName(),
                    itemRequest.getQuantity(),
                    itemRequest.getPrice(),
                    totalPrice
            );

            item.setOrderStatus(OrderStatus.PENDING);  

            totalAmount += totalPrice;
            product.setQuantity(product.getQuantity() - itemRequest.getQuantity());

            orderItemList.add(item);
        }

        newOrder.setItems(orderItemList);
        newOrder.setTotalAmount(totalAmount);

        // Save the order
        orderRepo.save(newOrder);

        // Notify the seller and send order confirmation email
        emailService.sendOrderPlacedEmail(newOrder.getEmail(), newOrder.get_id());

        return newOrder;
    }

    private void notifySeller(Order order) {
        Map<String, List<OrderItem>> sellerItemsMap = new HashMap<>();

        // Grouping OrderItems by Seller's email
        for (OrderItem item : order.getItems()) {
            String sellerEmail = sellerService.getSellerEmailByProductId(item.getProductId());
            sellerItemsMap.computeIfAbsent(sellerEmail, k -> new ArrayList<>()).add(item);
        }

        // Notifying each seller with a single email listing all relevant order items
        for (Map.Entry<String, List<OrderItem>> entry : sellerItemsMap.entrySet()) {
            String sellerEmail = entry.getKey();
            List<OrderItem> sellerItems = entry.getValue();

           
            String subject = "New Order Notification for Multiple Products";
            StringBuilder body = new StringBuilder("You have a new order for the following items:\n\n");

            for (OrderItem item : sellerItems) {
                body.append("Product: ").append(item.getProductName())
                        .append(", Quantity: ").append(item.getQuantity())
                        .append(", Total Price: ").append(item.getTotalPrice())
                        .append("\n");
            }

            // Send a single email per seller
            emailService.sendEmail(sellerEmail, subject, body.toString());
        }

    }

    public void cancelOrder(String orderId, String productId, String cancellationReason) throws Exception {
        // Retrieve the order by ID
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new Exception("Order not found: " + orderId));

        boolean itemFound = false;

       
        for (OrderItem orderItem : order.getItems()) {
            if (orderItem.getProductId().equals(productId)) {

                if (orderItem.getOrderStatus() != OrderStatus.PENDING) {
                    throw new Exception("Item cannot be cancelled: " + productId + " in Order " + orderId);
                }

                orderItem.setOrderStatus(OrderStatus.CANCELLED);

                Product product = productRepo.findById(orderItem.getProductId())
                        .orElseThrow(() -> new Exception("Product not found: " + orderItem.getProductId()));
                product.setQuantity(product.getQuantity() + orderItem.getQuantity());
                productRepo.save(product);

                orderItem.setReturnReason(cancellationReason);

                itemFound = true;

                // cancellation email for the specific product
                emailService.sendOrderItemCancelledEmail(order.getEmail(), productId, order.get_id(),cancellationReason);

                break; 
            }
        }

        // If no item was found with the provided productId
        if (!itemFound) {
            throw new Exception("Product not found in the order: " + productId);
        }

        orderRepo.save(order);
    }



    //    Customer want to view order
    public Order viewOrder(String orderId) throws Exception{
        return orderRepo.findById(orderId)
                .orElseThrow(()->new Exception("Order not found: "+orderId));
    }

//    Admin want to view order
    public List<Order> orders(){
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        String userName=authentication.getName();
        Optional<User> user=userRepo.findByUsername(userName);
        if(user.get().getRole()==Role.ADMIN) {
            return orderRepo.findAll();
        }
        return new ArrayList<>();
    }

//    Seller want to see his order list
    public List<Order> sellerOrder(String userId){
        return orderRepo.findByUserId(userId);

    }

//    Seller want to update any order
    public String updateOrder(String orderId, UpdateOrderRequest updateOrderRequest, String token) throws Exception {
    String userName = jwtService.extractUserName(token.replace("Bearer ", "").trim());
    User user = userRepo.findByUsername(userName)
            .orElseThrow(() -> new Exception("User not found"));

    if (!user.isBeASeller()) {
        throw new Exception("User is not a seller");
    }

    // Fetch the order
    Order order = orderRepo.findById(orderId)
            .orElseThrow(() -> new Exception("Order not found " + orderId));

    // Authorization check - check if the seller owns any of the products in the order
    if (!isAuthorizedToUpdateOrder(order, user)) {
        throw new Exception("You are not authorized to update this order.");
    }

    // Perform the update if applicable
    updateOrderDetails(order, updateOrderRequest);
    updateProductsWithEligibleUsers(order);

    orderRepo.save(order);
    return "Order updated successfully!";
}


    private boolean isAuthorizedToUpdateOrder(Order order, User user) {
        return order.getItems().stream()
                .anyMatch(item -> productRepo.findById(item.getProductId())
                        .map(product -> product.getSellerId().equals(user.get_id()))
                        .orElse(false));
    }

    private void updateOrderDetails(Order order, UpdateOrderRequest updateOrderRequest) {
       
        if (updateOrderRequest.getPhoneNumber() != null) {
            order.setPhoneNumber(updateOrderRequest.getPhoneNumber());
        }
        if (updateOrderRequest.getAddress() != null) {
            order.setAddress(updateOrderRequest.getAddress());
        }
        if (updateOrderRequest.getDeliveryDate() != null) {
            order.setDeliveryDate(updateOrderRequest.getDeliveryDate());
        }

        if (updateOrderRequest.getItems() != null) {
            for (UpdateOrderItemRequest itemRequest : updateOrderRequest.getItems()) {
                for (OrderItem orderItem : order.getItems()) {
                    if (orderItem.getProductId().equals(itemRequest.getProductId())) {
                        if (itemRequest.getOrderStatus() != null) {
                            orderItem.setOrderStatus(itemRequest.getOrderStatus());
                        }
                        if (itemRequest.getQuantity() > 0) {
                            orderItem.setQuantity(itemRequest.getQuantity());
                        }
                        break;  
                    }
                }
            }
        }
    }


    private void updateProductsWithEligibleUsers(Order order) throws Exception {
        for (OrderItem item : order.getItems()) {
            Product product = productRepo.findById(item.getProductId())
                    .orElseThrow(() -> new Exception("Product not found for order item " + item.getProductId()));

            // Add user to eligible list
            if (product.getEligibleUserIds() == null) {
                product.setEligibleUserIds(new ArrayList<>());  // Initialize if null
            }
            product.getEligibleUserIds().add(order.getUserId());
            //product.getEligibleUserIds().add(order.getUserId());
            productRepo.save(product);
        }
    }


    public List<CustomerDto> getCustomersWithOrders() {
        return orderRepo.findAll() // Fetch all orders
                .stream()
                .map(order -> new CustomerDto(order.getUserId(), order.getUserName())) // Map to DTO
                .distinct() // Remove duplicates
                .collect(Collectors.toList());
    }



    public boolean isUserEligibleForReview(String productId, String userId) throws Exception {
        // Find the product by product ID
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new Exception("Product not found"));

        // Check if the user is in the eligible user list
        return product.getEligibleUserIds().contains(userId);
    }


    public String addReview( String userId, ReviewRequest reviewRequest) throws Exception {
        // Check if the user is eligible to leave a review
        if (!isUserEligibleForReview(reviewRequest.getProductId(), userId)) {
            throw new Exception("You are not eligible to leave a review for this product.");
        }

        // Find the product by productId
        Product product = productRepo.findById(reviewRequest.getProductId())
                .orElseThrow(() -> new Exception("Product not found"));


        // Create the review object and add it to the product's reviews list
        Review review = new Review();
        review.setUserId(userId);
        review.setRating(reviewRequest.getRating());
        review.setComment(reviewRequest.getComment());
        review.setDate(new Date()); // Set the current date

        // Add the review to the product's review list
        product.getReviews().add(review);

        // Calculate the new average rating if needed
        double newAvgRating = product.getReviews().stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
        product.setAverageRating(newAvgRating);

        // Save the updated product
        productRepo.save(product);

        return "Review added successfully!";
    }


    public List<Order> viewAllOrder(String userId) {
        return orderRepo.findByUserId(userId);
    }

    public void updateOrderItemStatus(String orderId, String productId, OrderStatus newStatus) throws Exception {
        // Fetch the order by ID
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new Exception("Order not found"));

        boolean itemFound = false;
        for (OrderItem item : order.getItems()) {
            if (item.getProductId().equals(productId)) {
                // Update the status of the matching order item
                item.setOrderStatus(newStatus);
                itemFound = true;
                break;
            }
        }

        if (!itemFound) {
            throw new Exception("Product with ID " + productId + " not found in the order.");
        }

        // Save the updated order
        orderRepo.save(order);
    }

    public List<Order> getOrdersForSeller(List<String> productIds) {
        List<Order> allOrders = orderRepo.findAll();  

        List<Order> filteredOrders = allOrders.stream()
                .filter(order -> order.getItems().stream()
                        .anyMatch(item -> productIds.contains(item.getProductId())))
                .collect(Collectors.toList());

        return filteredOrders;
    }


}




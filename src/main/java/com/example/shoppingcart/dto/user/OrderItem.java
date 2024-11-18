package com.example.shoppingcart.dto.user;

import com.example.shoppingcart.dto.OrderStatus;
import com.example.shoppingcart.dto.Review;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    @Id
    private String id;



//    PRODUCT DETAILS
    private String productId;
    private String productName;
    private double price;
    private int quantity;
    private double totalPrice;
    private String size;
    private String color;

    private OrderStatus orderStatus;

    private boolean isReturned;
    private boolean isExchanged;
    private String cancellationReason;
    private String returnReason;
    private Review review;




    public OrderItem(String productId, String productName, int quantity, double price) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.totalPrice = quantity * price;
    }

    public OrderItem(String productId, String productName, int quantity, double price, double totalPrice) {
        this.productId=productId;
        this.productName=productName;
        this.quantity=quantity;
        this.price=price;
        this.totalPrice=totalPrice;
    }

    public void calculateSubtotal() {
        this.totalPrice = this.quantity * this.price;
    }


    public void addReview(Integer rating, String reviewComment) {
        review.setComment(reviewComment);
        review.setRating(rating);
    }
}

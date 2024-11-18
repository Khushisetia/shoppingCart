package com.example.shoppingcart.models;

import com.example.shoppingcart.dto.Review;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Document(collection = "product")
public class Product{
@Id
private String id; // Unique identifier for the product

private String name; // Name of the product

private String description; // Brief description of the product

private double price; // Price of the product

private int quantity; // Available quantity of the product

private String status; // Status of the product like "Available", "Out of Stock", "Shipped", "Delivered"

private String category; // Category for better product classification (e.g., Electronics, Clothing, etc.)

private String imageName; // Name of the image file

private String imageType; // Type of the image (e.g., "image/png", "image/jpeg")

private byte[] imageData; // Byte array for storing the image data

@CreatedDate
private LocalDateTime createdAt; // Timestamp when the product was added

@LastModifiedDate
private LocalDateTime updatedAt; // Timestamp when the product details were last updated

private LocalDateTime expiryDate; // Expiry date for applicable products (e.g., Food, Body Essentials)

private String sellerId; // Add this to link the product to the seller

private String sellerName;

private List<String> customerIds=new ArrayList<>(); // List of customer IDs who bought the product

private List<Review> reviews = new ArrayList<>(); // List of reviews{

private List<String> eligibleUserIds ;

private double averageRating;

public Product(String productId,String productName,double price,int quantity){
    this.id=productId;
    this.name=productName;
    this.price=price;
    this.quantity=quantity;
}
}

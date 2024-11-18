package com.example.shoppingcart.dto.user;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WishlistItem {
    //private String userId;
    private String imageName; // Name of the image file

    private String imageType; // Type of the image (e.g., "image/png", "image/jpeg")

    private byte[] imageData; // Byte array for storing the image data
    private String productId;
    private String productName;
    private double price;



    // You can add more fields as necessary
}


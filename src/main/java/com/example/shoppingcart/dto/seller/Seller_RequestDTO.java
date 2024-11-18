package com.example.shoppingcart.dto.seller;

import com.example.shoppingcart.dto.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Seller_RequestDTO {
    private String password;
    private Gender gender; // Gender of the user
    private String phoneNumber; // Phone number of the user
    private String email; // Email of the user
    private String sellerName;
    private String businessName;
    private String businessDetails;


}

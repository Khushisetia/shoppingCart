package com.example.shoppingcart.models;

import com.example.shoppingcart.dto.Gender;
import com.example.shoppingcart.dto.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "sellers")
public class Seller {

    @Id
    private String userId;
    private String sellerName;
    private String email;
    private String businessName;
    private String businessDetails;
    private Role role;
    private String password;
    private String phoneNumber;
    private Gender gender;
    //private String userId;

}

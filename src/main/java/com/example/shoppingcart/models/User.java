package com.example.shoppingcart.models;

import com.example.shoppingcart.dto.Gender;
import com.example.shoppingcart.dto.Role;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String _id;
    @Indexed(unique = true)
    private String username;
    @NotNull
    private String password;
    private Role role; // e.g., ADMIN, USER
    private String fullName; // Full name of the user
    private String email; // Email address of the user
    private String phoneNumber; // Phone number (optional)
    private String address; // Address (optional)
    @CreatedDate
    private LocalDateTime createdDate; // Date when the user was created
    @LastModifiedDate
    private LocalDateTime lastLoginDate; // Date of the last login
    private Gender gender;

    private boolean beASeller=false;



    public User(String userId,String username,String email){
        this._id=userId;
        this.username=username;
        this.email=email;
    }
}

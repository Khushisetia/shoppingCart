package com.example.shoppingcart.dto.user;

import com.example.shoppingcart.dto.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderRequest {
    private String userId;
    private String userName;
    private String email;
    private String phoneNumber;
    private List<OrderItemRequest> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Seller_Request {
        @Id
        private String id; // You can also use @GeneratedValue for auto-generated IDs
        private String username;
        private Gender gender; // Gender of the user
    //    @Indexed(unique = true)
        private String email; // Email of the user
        private String phoneNumber; // Phone number of the user

        private String sellerName;

        private String businessName;
        private String businessDetails;

        private LocalDateTime requestDate;
        private boolean approved;
        private String password;

    }
}

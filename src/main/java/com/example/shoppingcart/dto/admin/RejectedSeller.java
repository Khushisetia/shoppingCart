package com.example.shoppingcart.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "rejected_sellers")
public class RejectedSeller {
    @Id
    private String id;
    private String requestId;
    private String sellerName;
    private String email;
    private String rejectionReason;
    private LocalDateTime rejectionDate;


}
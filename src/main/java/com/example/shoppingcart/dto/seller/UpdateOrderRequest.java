package com.example.shoppingcart.dto.seller;



import com.example.shoppingcart.dto.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UpdateOrderRequest {
    private String id;
    private String phoneNumber;
    private String address;// You can add more fields as needed
    private String productId;
    private OrderStatus orderStatus;
    private LocalDateTime DeliveryDate;
    private List<UpdateOrderItemRequest> items;  // List of items to update

}

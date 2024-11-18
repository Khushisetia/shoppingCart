    package com.example.shoppingcart.models;

    import com.example.shoppingcart.dto.user.OrderItem;
    import lombok.AllArgsConstructor;
    import lombok.Data;
    import lombok.NoArgsConstructor;
    import org.springframework.data.annotation.CreatedDate;
    import org.springframework.data.annotation.Id;
    import org.springframework.data.mongodb.core.mapping.Document;

    import java.time.LocalDateTime;
    import java.util.ArrayList;
    import java.util.List;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Document(collection = "order")
    public class Order {
        @Id
        private String _id;

    //    User Details
        private String userId;
        private String userName;
        private String email;
        private String phoneNumber;
      //  private OrderStatus status;
        private String Address;

        @CreatedDate
        private LocalDateTime orderDate;
        private LocalDateTime paymentDate;
        private LocalDateTime shippingDate;
        private LocalDateTime deliveryDate;


        private List<OrderItem> items=new ArrayList<>();
        private double totalAmount;


        private boolean isReturned;
        private boolean isExchanged;
        private String returnReason;



    }

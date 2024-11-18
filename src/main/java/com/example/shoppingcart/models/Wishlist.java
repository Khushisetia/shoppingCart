package com.example.shoppingcart.models;

import com.example.shoppingcart.dto.user.WishlistItem;
    import lombok.AllArgsConstructor;
    import lombok.Data;
    import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Document(collection = "wishlist")
    public class Wishlist {
        @Id
        private String id;
        private String userId;
        private Set<WishlistItem> product;
    public Wishlist(String userId, Set<WishlistItem> products) {
        this.userId = userId;
        this.product = products;
    }

}

package com.example.shoppingcart.repo;

import com.example.shoppingcart.models.Wishlist;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface WishlistRepo extends MongoRepository<Wishlist,String> {
    Optional<Wishlist> findByUserId(String userId);
}

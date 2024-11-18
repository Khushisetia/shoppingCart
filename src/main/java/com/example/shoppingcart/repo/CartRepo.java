package com.example.shoppingcart.repo;

import com.example.shoppingcart.models.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CartRepo extends MongoRepository<Cart,String> {

    Optional<Cart> findByUserId(String userId);


}

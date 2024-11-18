package com.example.shoppingcart.repo;

import com.example.shoppingcart.dto.user.OrderRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface Seller_RequestRepo extends MongoRepository<OrderRequest.Seller_Request, String> {
    Optional<OrderRequest.Seller_Request> findByUsername(String username);
}

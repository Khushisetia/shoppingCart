package com.example.shoppingcart.repo;

import com.example.shoppingcart.models.Seller;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SellerRepo extends MongoRepository<Seller,String> {


    Optional<Seller> findBySellerName(String sellerName);

    Optional<Seller> findByUserId(String userId);

    void deleteBySellerName(String SellerName);

    void deleteByUserId(String id);

    Optional<Seller> findByEmail(String email);

}


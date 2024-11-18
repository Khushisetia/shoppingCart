package com.example.shoppingcart.repo;

import com.example.shoppingcart.dto.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepo extends MongoRepository<Review,String> {
    List<Review> findByProductId(String productId);
}

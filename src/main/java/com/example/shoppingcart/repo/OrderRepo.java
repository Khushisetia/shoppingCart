package com.example.shoppingcart.repo;

import com.example.shoppingcart.models.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepo extends MongoRepository<Order,String> {
    List<Order> findByUserId(String userId);

    List<Order> findAll();

    @Query("{ 'userId': ?0, 'items.productId': ?1 }")
    Optional<Order> findByUserIdAndProductId(String userId, String productId);

    // In OrderRepository
    @Query("{ 'orderItems.productId' : { $in: ?0 } }")
    List<Order> findByProductIds(List<String> productIds);


}

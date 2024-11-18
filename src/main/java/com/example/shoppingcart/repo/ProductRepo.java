package com.example.shoppingcart.repo;

import com.example.shoppingcart.models.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepo extends MongoRepository<Product,String> {

    List<Product> findByNameContainingIgnoreCase(String productName);

    List<Product> findBySellerName(String sellerName);

    List<Product> findBySellerId(String sellerId);

    List<Product> findByCategory(String category);


    void deleteBySellerId(String id);
}

package com.example.shoppingcart.repo;

import com.example.shoppingcart.dto.admin.RejectedSeller;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RejectedSellerRepo extends MongoRepository<RejectedSeller,String> {
}

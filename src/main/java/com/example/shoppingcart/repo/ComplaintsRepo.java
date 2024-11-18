package com.example.shoppingcart.repo;

import com.example.shoppingcart.models.Complaints;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ComplaintsRepo extends MongoRepository<Complaints,String> {

   List<Complaints> findByUserId(String userId);
}

package com.example.shoppingcart.repo;

import com.example.shoppingcart.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends MongoRepository<User,Integer> {

    @Query("{'username':?0}")
    Optional<User> findByUsername(String username);

    Optional<User> findBy_id(String _id);

    List<User> findByRole(String role);

    boolean existsByUsername(String username);

    void deleteByUsername(String username);
}

package com.example.shoppingcart.service;

import com.example.shoppingcart.dto.user.OrderItem;
import com.example.shoppingcart.dto.OrderStatus;
import com.example.shoppingcart.dto.Role;
import com.example.shoppingcart.models.Order;
import com.example.shoppingcart.models.User;
import com.example.shoppingcart.repo.OrderRepo;
import com.example.shoppingcart.repo.ProductRepo;
import com.example.shoppingcart.repo.SellerRepo;
import com.example.shoppingcart.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;


@Service
public class MyUserService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private SellerRepo sellerRepo;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    JWTService jwtService;

    @Autowired
    AuthenticationManager authenticationManager;

    private BCryptPasswordEncoder encoder=new BCryptPasswordEncoder(12);

    public void addUser(User user) {

        if (!isValidEmail(user.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }

        if (userRepo.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("User with this username already exists");
        }
        String encryptedPassword = encoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);
//        Set<Role> roles = new HashSet<>();
//        roles.add(Role.USER); // Default role is USER
//        user.setRole(roles);
        user.setRole(Role.USER);
        userRepo.save(user);
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    public String verify(User user) {
        Authentication authentication=
                authenticationManager
                        .authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(),user.getPassword()));
        if(authentication.isAuthenticated()) {
            return jwtService.generateToken(user.getUsername());
        }


        return "Fail";
    }

    public boolean deleteUserByUsername(String username) {

        User user = userRepo.findByUsername(username).orElse(null);
        if (user != null) {
            cancelUserOrders(user);


            if (user.isBeASeller()) {
                productRepo.deleteBySellerId(user.get_id());

                sellerRepo.deleteByUserId(user.get_id());
            }

            userRepo.deleteByUsername(username);
            return true;
        }
        return false;
    }

    private void cancelUserOrders(User user) {
        // Find all orders associated with the user
        List<Order> orders = orderRepo.findByUserId(user.get_id());

        // Iterate over each order and cancel the items
        for (Order order : orders) {
            // Iterate over each OrderItem in the order
            for (OrderItem orderItem : order.getItems()) {
                // Update the order status for each item
                orderItem.setOrderStatus(OrderStatus.CANCELLED);
                orderItem.setCancellationReason("Account deleted");
            }

            // Save the updated order with the updated OrderItems
            orderRepo.save(order);  // This will save both the order and its modified OrderItems
        }
    }


}


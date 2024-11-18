package com.example.shoppingcart.service;

import com.example.shoppingcart.dto.user.OrderRequest;
import com.example.shoppingcart.dto.admin.RejectedSeller;
import com.example.shoppingcart.dto.Role;
import com.example.shoppingcart.dto.seller.Seller_login;
import com.example.shoppingcart.exceptions.UserNotFoundException;
import com.example.shoppingcart.models.Product;
import com.example.shoppingcart.models.Seller;
import com.example.shoppingcart.models.User;
import com.example.shoppingcart.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SellerService {

    @Autowired
    private SellerRepo sellerRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private Seller_RequestRepo sellerRequestRepo;

    @Autowired
    private OrderRepo orderRepo;



    // Register a user as a seller
       public String registerAsASeller(Seller seller, String token) {
            String userName = jwtService.extractUserName(   token); 
            //String userId = extractUserIdFromJWT(token);
            System.out.println("Extracted User ID: " + userName);
            Optional<User> userOptional = userRepo.findByUsername(userName);
            if (userOptional.isPresent()) {
                User user = userOptional.get();

                if (!user.isBeASeller()) {
                    seller.setRole(Role.SELLER);
                    seller.setUserId(user.get_id()); //  user ID for seller
                    sellerRepo.save(seller);
                    user.setBeASeller(true);
                    userRepo.save(user);
                    return "User successfully registered as a seller!";
                } else {
                    return "User is already registered as a seller.";
                }
            }
            return "User not found.";
        }
    
    // Approve request
      public void approveRequest(String requestId) throws Exception {
        Optional<OrderRequest.Seller_Request> requestOptional = sellerRequestRepo.findById(requestId);
          if (requestOptional.isPresent()) {
            OrderRequest.Seller_Request request = requestOptional.get();
            request.setApproved(true);
            sellerRequestRepo.save(request); 
            Seller seller = new Seller();
            seller.setUserId(request.getId());
            seller.setPassword(request.getPassword());
            seller.setBusinessDetails(request.getBusinessDetails());
            seller.setBusinessName(seller.getSellerName());
            seller.setSellerName(request.getUsername());
            seller.setEmail(request.getEmail());
            seller.setPhoneNumber(request.getPhoneNumber());
            seller.setGender(request.getGender());

            sellerRepo.save(seller); // Save the new Seller entity
        } else {
            throw new Exception("Request not found");
        }
    }
    

    // Reject a seller request
    public String getSellerEmailByProductId(String productId) {

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        
        Optional<Seller> seller = sellerRepo.findBySellerName(product.getSellerName());

        if (seller != null) {
            return seller.get().getEmail();
        }
        throw new RuntimeException("Seller not found");
    }

    @Autowired
    private RejectedSellerRepo rejectedSellerRepository;

    public void rejectRequest(String requestId,String reason) {
        OrderRequest.Seller_Request sellerRequest = findSellerRequestById(requestId);

        RejectedSeller rejectedSeller = new RejectedSeller();
        rejectedSeller.setRequestId(requestId);
        rejectedSeller.setSellerName(sellerRequest.getUsername());
        rejectedSeller.setEmail(sellerRequest.getEmail());
        rejectedSeller.setRejectionReason(reason); // Optional: Provide reason
        rejectedSeller.setRejectionDate(LocalDateTime.now());

        rejectedSellerRepository.save(rejectedSeller);
        sellerRequestRepo.deleteById(requestId);
    }

   public OrderRequest.Seller_Request findSellerRequestById(String requestId) {
        Optional<OrderRequest.Seller_Request> sellerRequest = sellerRequestRepo.findById(requestId);
        return sellerRequest.orElse(null); 
    }

    public String loginASASeller(Seller_login sellerLogin) {
        String email = sellerLogin.getEmail();
        String password = sellerLogin.getPassword();

        Optional<Seller> sellerOptional = sellerRepo.findByEmail(email);
        if (!sellerOptional.isPresent()) {
            throw new UserNotFoundException("Seller not found");
        }

        Seller seller = sellerOptional.get();

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (!passwordEncoder.matches(password, seller.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return "Seller login successful";
    }



}

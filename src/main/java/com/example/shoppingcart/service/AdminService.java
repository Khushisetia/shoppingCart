package com.example.shoppingcart.service;

import com.example.shoppingcart.models.Product;
import com.example.shoppingcart.models.Seller;
import com.example.shoppingcart.models.User;
import com.example.shoppingcart.repo.ProductRepo;
import com.example.shoppingcart.repo.SellerRepo;
import com.example.shoppingcart.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private UserRepo userRepo;


    @Autowired
    private SellerRepo sellerRepo;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private EmailService emailService;

    public List<User> getAllUsers() {
        return userRepo.findByRole("USER");
    }

    public List<Seller> getAllSellers() {
        return sellerRepo.findAll();
    }

    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    public List<Product> findBySeller(String name) {
        return productRepo.findBySellerName(name);
    }


    public String deleteProduct(String productId, String reason) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
       String sellerName=product.getSellerName();
        Optional<Seller> seller = sellerRepo.findBySellerName(sellerName);
        String sellerEmail=seller.get().getEmail();

        String subject = "Product Deletion Notification";
        String body = "Dear " + seller.get().getSellerName() + ",\n\n"
                + "We regret to inform you that your product '" + product.getName() + "' has been deleted for the following reason: "
                + reason + ".\n\nThank you for your understanding.\n\nBest regards,\nAdmin Team";
        System.out.println(sellerEmail);
        System.out.println(subject +""+body);
        emailService.sendEmail(sellerEmail, subject, body);

        // Delete the product
        productRepo.delete(product);
        return "Product deleted";
    }


}
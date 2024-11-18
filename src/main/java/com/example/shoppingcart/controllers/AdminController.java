package com.example.shoppingcart.controllers;

import com.example.shoppingcart.dto.user.CustomerDto;
import com.example.shoppingcart.dto.user.OrderRequest;
import com.example.shoppingcart.dto.admin.RejectedSeller;
import com.example.shoppingcart.models.*;
import com.example.shoppingcart.repo.RejectedSellerRepo;
import com.example.shoppingcart.repo.SellerRepo;
import com.example.shoppingcart.repo.Seller_RequestRepo;
import com.example.shoppingcart.repo.UserRepo;
import com.example.shoppingcart.service.AdminService;
import com.example.shoppingcart.service.ComplaintService;
import com.example.shoppingcart.service.OrderService;
import com.example.shoppingcart.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders; // Import for HttpHeaders


import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/admin/")

public class AdminController {
    @Autowired
    private AdminService adminService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private Seller_RequestRepo sellerRequestRepo;

    @Autowired
    private SellerService sellerService;

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private UserRepo userRepo;


    @Autowired
    private SellerRepo sellerRepo;

    @Autowired
    private RejectedSellerRepo rejectedSellerRepo;


    @GetMapping("AllUsers")
    public List<User> getAllUsers(){
        return adminService.getAllUsers();
    }

    @GetMapping("AllSellers")
    public List<Seller> getAllSellers(){
        return adminService.getAllSellers();
    }

    @GetMapping("AllProducts")
    public List<Product> getAllProduct(){
        return adminService.getAllProducts();
    }

    @GetMapping("ProductBySellerName")
    public List<Product> findBYSellerName(@RequestParam String sellerName){
        return adminService.findBySeller(sellerName);
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<String> deleteProduct(
            @PathVariable String productId,
            @RequestParam String reason) {
        try {
            adminService.deleteProduct(productId, reason);
            return ResponseEntity.ok("Product deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("viewAllOrders")
    public List<Order> getAllOrders(){
        return orderService.orders();
    }



    @GetMapping("/downloadCustomers")
    public ResponseEntity<byte[]> downloadCustomers() {
        List<CustomerDto> customers = orderService.getCustomersWithOrders();

        // Create CSV data
        StringBuilder csvData = new StringBuilder();
        csvData.append("UserId,UserName\n"); // Header

        for (CustomerDto customer : customers) {
            csvData.append(customer.getUserId())
                    .append(",")
                    .append(customer.getUserName())
                    .append("\n");
        }

        // Convert CSV data to byte array
        byte[] bytes = csvData.toString().getBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=customers.csv");

        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @GetMapping("sellerRequests")
    public ResponseEntity<List<OrderRequest.Seller_Request>> getAllSellerRequests() {
        List<OrderRequest.Seller_Request> requests = sellerRequestRepo.findAll();
        return ResponseEntity.ok(requests);
    }

    @PutMapping("/seller-requests/{requestId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveSellerRequest(@PathVariable("requestId") String requestId) {
        try {

            Optional<User> userOptional = userRepo.findBy_id(requestId);

            // If the user is not found, return a 404 error
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
            }

            
            User user = userOptional.get();

            
            user.setBeASeller(true);

            Optional<OrderRequest.Seller_Request> sellerOptional = sellerRequestRepo.findById(requestId);

            // If no seller is found, return a 404 error
            if (sellerOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Seller request not found.");
            }



            // Approve the seller request using the service
            sellerService.approveRequest(requestId);

            // Save the updated user object
            userRepo.save(user);
            return ResponseEntity.ok("Seller request approved successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Seller request not found.");
        }
    }

    @DeleteMapping("/seller-requests/{requestId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rejectSellerRequest(@PathVariable String requestId,@RequestParam String reason) {
        sellerService.rejectRequest(requestId,reason);
        return ResponseEntity.ok("Seller request rejected successfully.");
    }

    //response
    @PutMapping("/complaintResponse/{complaintId}")
    public ResponseEntity<String> responseComplaint(@PathVariable String complaintId,@RequestParam String response) throws Exception {
        complaintService.response(complaintId,response);
        return ResponseEntity.ok("Response registered");

    }

    //viewAllComplaints
     @GetMapping("/viewAllComplaints")
    public List<Complaints> viewAllComplaints(){
        return complaintService.viewComplaints();
     }

     @GetMapping ("/viewComplaint/{userId}")
    public Optional<Complaints> viewComplaintById(@PathVariable String userId){
        return complaintService.viewById(userId);
     }

     @GetMapping("/view/RejectedSellers")
    public List<RejectedSeller> rejectedSeller(){
        return rejectedSellerRepo.findAll();
     }
}

package com.example.shoppingcart.service;

import com.example.shoppingcart.models.Product;
import com.example.shoppingcart.repo.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    ProductRepo productRepo;

    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        if (to == null || subject == null || body == null) {
            throw new IllegalArgumentException("Email parameters cannot be null");
        }

        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
    public void sendOrderPlacedEmail(String to, String orderId) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Order Confirmation - Order ID: " + orderId);
        message.setText("Dear Customer,\n\nYour order with ID " + orderId + " has been successfully placed.\n\nThank you for shopping with us!");

        mailSender.send(message);
    }



    public void sendReviewRequestEmail(String customerEmail, String productId) {
        // Fetch product details using the productId
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Create the email message
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(customerEmail);
        message.setSubject("Your Order has been Delivered!");

        // Create a personalized message with product details
        String messageBody = String.format(
                "Dear Customer,\n\nYour order containing the product '%s' has been delivered. " +
                        "Please take a moment to leave a review for the product. Your feedback helps us improve our service.\n\n" +

                        "Thank you for shopping with us!",
                product.getName()  // Assuming the product has a name
        );
        message.setText(messageBody);

        // Send the email
        mailSender.send(message);
    }

    public void sendOrderItemCancelledEmail(String to, String orderId, String productId, String cancellationReason) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setSubject("Order Item Cancellation - Order ID: " + orderId);
        message.setText("Dear Customer,\n\n"
                + "Your order with ID " + orderId + " has been updated with the following cancellation:\n\n"
                + "Product ID: " + productId + "\n"
                + "Cancellation Reason: " + cancellationReason + "\n\n"
                + "If you have any questions, please contact support.\n\n"
                + "Thank you for shopping with us.");

        mailSender.send(message);
    }

}

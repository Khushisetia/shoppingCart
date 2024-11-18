package com.example.shoppingcart.service;

import com.example.shoppingcart.exceptions.ResourcetNotFoundException;
import com.example.shoppingcart.models.Product;
import com.example.shoppingcart.models.Seller;
import com.example.shoppingcart.models.User;
import com.example.shoppingcart.repo.OrderRepo;
import com.example.shoppingcart.repo.ProductRepo;
import com.example.shoppingcart.repo.SellerRepo;
import com.example.shoppingcart.repo.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Slf4j
@Transactional
public class ProductService {
    @Autowired
    ProductRepo productRepo;

    @Autowired
    SellerRepo sellerRepo;

    @Autowired
    JWTService jwtService;

    @Autowired
    UserRepo userRepo;

    @Autowired
    OrderRepo orderRepo;

//    public Product addProducts(Product product,MultipartFile imageFile,String token) throws IOException {
//        String userName = jwtService.extractUserName(token);
//        Optional<User> user = userRepo.findByUsername(userName);
//
//
//        if (user.isPresent()) {
//            String userId = user.get().get_id();
//            Optional<Seller> seller = sellerRepo.findByUserId(userId);
//            if (seller.isPresent()) {
//                String sellerName = seller.get().getSellerName();
//                product.setSellerId(sellerName);
//                if (imageFile != null && !imageFile.isEmpty()) {
//                    product.setImageName(imageFile.getOriginalFilename());
//                    product.setImageType(imageFile.getContentType());
//                    product.setImageData(imageFile.getBytes()); // Convert MultipartFile to byte array
//                }
//
//                // Set timestamps
//                product.setCreatedAt(LocalDateTime.now());
//                product.setUpdatedAt(LocalDateTime.now());
//
//                return productRepo.save(product);
//                log.info("Product added by Seller: {} | Product Name: {} | Category: {} | Price: {}",
//                        sellerName, product.getName(), product.getCategory(), product.getPrice());
//            } else {
//                log.warn("Attempt failed to add product.Seller with userId '{}' no found in database.", userId);
//            }
//        } else {
//            return new Product();
//            log.warn("Attempt to add product failed. Seller '{}' not found in the database.", userName);
//            throw new RuntimeException("User not found");
//        }
//
//
//    }
//public Product addProducts(Product product, MultipartFile imageFile, String token) throws IOException {
//    String userName = jwtService.extractUserName(token);
//    Optional<User> user = userRepo.findByUsername(userName);
//
//    if (user.isPresent()) {
//        String userId = user.get().get_id();
//        Optional<Seller> seller = sellerRepo.findByUserId(userId);
//        if (seller.isPresent()) {
//            String sellerName = seller.get().getSellerName();
//            product.setSellerId(sellerName);
//
//            if (imageFile != null && !imageFile.isEmpty()) {
//                product.setImageName(imageFile.getOriginalFilename());
//                product.setImageType(imageFile.getContentType());
//                product.setImageData(imageFile.getBytes()); // Convert MultipartFile to byte array
//            }
//
//            // Set timestamps
//            product.setCreatedAt(LocalDateTime.now());
//            product.setUpdatedAt(LocalDateTime.now());
//
//            // Save the product
//            Product savedProduct = productRepo.save(product);
//            log.info("Product added by Seller: {} | Product Name: {} | Category: {} | Price: {}",
//                    sellerName, product.getName(), product.getCategory(), product.getPrice());
//            return savedProduct; // Return the saved product
//        } else {
//            log.warn("Attempt failed to add product. Seller with userId '{}' not found in database.", userId);
//            throw new RuntimeException("Seller not found"); // Throw an exception instead of returning
//        }
//    } else {
//        log.warn("Attempt to add product failed. User '{}' not found in the database.", userName);
//        throw new RuntimeException("User not found"); // Throw an exception instead of returning an empty product
//    }
//}

    public Product addProduct(Product product, MultipartFile file) throws Exception {
         //Get the authenticated user's details
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // This gets the username from the JWT

        // Verify if the user is a seller
        Optional<User> user = userRepo.findByUsername(username);
        if (user == null || !user.get().isBeASeller())
            throw new Exception("User is not authorized to add products.");


        product.setSellerId(user.get().get_id());// Assuming you have a method to get user ID

        Optional<Seller> seller=sellerRepo.findByUserId(user.get().get_id());
        String sellerName=seller.get().getSellerName();
        product.setSellerName(sellerName.toString());

        // Handle the image upload
        try {
            // Set the image properties
            product.setImageName(file.getOriginalFilename());
            product.setImageType(file.getContentType());
            product.setImageData(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to store image data: " + e.getMessage());
        }

        // Save the product in the database
        return productRepo.save(product);
    }




//    public Product addProduct(Product product, List<MultipartFile> files) throws Exception {
//        // Get authenticated user details
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String username = authentication.getName();
//
//        Optional<User> user = userRepo.findByUsername(username);
//        if (user.isEmpty() || !user.get().isBeASeller()) {
//            throw new Exception("User is not authorized to add products.");
//        }
//
//        product.setSellerId(user.get().get_id());
//        Optional<Seller> seller = sellerRepo.findByUserId(user.get().get_id());
//        product.setSellerName(seller.get().getSellerName());
//
//        try {
//            List<Image> images = new ArrayList<>();
//            for (MultipartFile file : files) {
//                Image image = new Image();
//                image.setName(file.getOriginalFilename());
//                image.setType(file.getContentType());
//                image.setData(file.getBytes());
//                images.add(image);
//            }
//            product.setImages(images); // Set images in Product
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to store image data: " + e.getMessage());
//        }
//
//        return productRepo.save(product);
//    }

        public List<Product> searchByProductName(String name){
              return productRepo.findByNameContainingIgnoreCase(name);


        }

        public List<Product> searchBySellerId(String sellerId){
        return productRepo.findBySellerId(sellerId);
        }

        public List<Product> searchBySellerName(String sellerName){
        return productRepo.findBySellerName(sellerName);
        }

    public String updationOfProducts(String id, Product updatedProduct) {
        Product existingProduct = productRepo.findById(id)
                .orElseThrow(() -> new ResourcetNotFoundException("Product does not exist"));

        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setQuantity(updatedProduct.getQuantity());
        productRepo.save(existingProduct);
        return "Product updated successfully";
    }


    public List<String> getProductIdsBySellerId(String sellerId) {
        // Fetch all products for the given sellerId
        List<Product> products = productRepo.findBySellerId(sellerId);

        // Extract and return the list of product IDs
        return products.stream()
                .map(Product::getId)  // Assuming Product has a method getId() that returns product ID
                .collect(Collectors.toList());
    }

    public String deleteProductById(String productId) {
        Product existingProduct = productRepo.findById(productId).orElse(null);
        if (existingProduct == null) {
            return "Product not found";
        }

        // Step 3: Log the reason for deletion (this could also be sent to an admin or stored in a log file)
       // System.out.println("Deleting product: " + productId + ", Reason: " + reason);

        // Step 4: Delete the product
        productRepo.deleteById(productId);

        return "Product deleted successfully";
    }

    public Product getProductById(String productId) {
        return productRepo.findById(productId).orElse(null);
    }

    public boolean isProductAvailable(String productId, int quantity) {
          Optional<Product> product=productRepo.findById(productId);

          if(product!=null){
              return product.get().getQuantity()>=quantity;
          }
          return false;

    }

    public ResponseEntity<List<Product>> viewProducts(String sellerId) {
        List<Product> products = productRepo.findBySellerId(sellerId);  // assuming productRepo has this method

        // Check if any products were found
        if (products.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // No products found
        }

        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    public Optional<Product> findProductById(String productId) {
        return productRepo.findById(productId);
    }


//    public void addReview(String productId, Review review) throws Exception {
//        Product product = productRepo.findById(productId)
//                .orElseThrow(() -> new Exception("Product not found"));
//        product.getReviews().add(review); // Add the review to the list
//        productRepo.save(product); // Save the updated product
//    }

    public List<Product> findProductsByCategory(String category) {
        return productRepo.findByCategory(category);
    }


    public Optional<Product> searchById(String productId) {
        return productRepo.findById(productId);
    }
}
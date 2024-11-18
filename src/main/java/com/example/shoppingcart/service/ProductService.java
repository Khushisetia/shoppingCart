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


    public Product addProduct(Product product, MultipartFile file) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); 

        // Verifying if the user is a seller
        Optional<User> user = userRepo.findByUsername(username);
        if (user == null || !user.get().isBeASeller())
            throw new Exception("User is not authorized to add products.");


        product.setSellerId(user.get().get_id());

        Optional<Seller> seller=sellerRepo.findByUserId(user.get().get_id());
        String sellerName=seller.get().getSellerName();
        product.setSellerName(sellerName.toString());

       
        try {
            product.setImageName(file.getOriginalFilename());
            product.setImageType(file.getContentType());
            product.setImageData(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to store image data: " + e.getMessage());
        }

        return productRepo.save(product);
    }






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
        List<Product> products = productRepo.findBySellerId(sellerId);

        return products.stream()
                .map(Product::getId)  // Assuming Product has a method getId() that returns product ID
                .collect(Collectors.toList());
    }

    public String deleteProductById(String productId) {
        Product existingProduct = productRepo.findById(productId).orElse(null);
        if (existingProduct == null) {
            return "Product not found";
        }

       
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
        List<Product> products = productRepo.findBySellerId(sellerId);  

       
        if (products.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // No products found
        }

        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    public Optional<Product> findProductById(String productId) {
        return productRepo.findById(productId);
    }




    public List<Product> findProductsByCategory(String category) {
        return productRepo.findByCategory(category);
    }


    public Optional<Product> searchById(String productId) {
        return productRepo.findById(productId);
    }
}

package com.example.shoppingcart.controllers;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.example.shoppingcart.dto.user.CartItem;
import com.example.shoppingcart.models.Product;
import com.example.shoppingcart.models.User;
import com.example.shoppingcart.repo.UserRepo;
import com.example.shoppingcart.service.CartService;
import com.example.shoppingcart.service.ProductService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class CartControllerTest {

    @Mock
    private CartService cartService;

    @Mock
    private UserRepo userRepo;

    @Mock
    private ProductService productService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private CartController cartController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("testUser");
    }

    @Test
    public void testAddToCart_UserNotFound() {
        when(userRepo.findByUsername("testUser")).thenReturn(Optional.empty());
        CartItem cartItem = new CartItem("productId", "productName", 100.0, 2);
        ResponseEntity<String> response = cartController.addToCart(cartItem);
        assertEquals("User not found.", response.getBody());
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    public void testAddToCart_ProductNotFound() {
        User mockUser = new User("testUser", "userId", "test@example.com");
        when(userRepo.findByUsername("testUser")).thenReturn(Optional.of(mockUser));
        when(productService.findProductById("productId")).thenReturn(Optional.empty());

        CartItem cartItem = new CartItem("productId", "productName", 100.0, 2);
        ResponseEntity<String> response = cartController.addToCart(cartItem);
        assertEquals("Product not found.", response.getBody());
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    public void testAddToCart_InsufficientStock() {
        User mockUser = new User("testUser", "userId", "test@example.com");
        when(userRepo.findByUsername("testUser")).thenReturn(Optional.of(mockUser));
        Product mockProduct = new Product("productId", "productName", 10.0, 50);
        when(productService.findProductById("productId")).thenReturn(Optional.of(mockProduct));

        CartItem cartItem = new CartItem("productId", "productName", 10.0, 100);
        ResponseEntity<String> response = cartController.addToCart(cartItem);
        assertEquals("Insufficient stock for productName", response.getBody());
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    public void testAddToCart_PriceMismatch() {
        User mockUser = new User("testUser", "userId", "test@example.com");
        when(userRepo.findByUsername("testUser")).thenReturn(Optional.of(mockUser));
        Product mockProduct = new Product("productId", "Product Name", 10.0, 50);
        when(productService.findProductById("productId")).thenReturn(Optional.of(mockProduct));

        CartItem cartItem = new CartItem("productId", "Product Name", 15.0, 2);
        ResponseEntity<String> response = cartController.addToCart(cartItem);
        assertEquals("Price mismatch. Please update your cart.", response.getBody());
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    public void testAddToCart_Success() {
        User mockUser = new User("userId1234", "testUser", "test@example.com");
        when(userRepo.findByUsername("testUser")).thenReturn(Optional.of(mockUser));
        Product mockProduct = new Product("productId", "Product Name", 10.0, 50);
        when(productService.findProductById("productId")).thenReturn(Optional.of(mockProduct));

        CartItem cartItem = new CartItem("productId", "Product Name", 10.0, 2);
        when(cartService.addItemToCart(eq("userId1234"), any(CartItem.class))).thenReturn(true);

        ResponseEntity<String> response = cartController.addToCart(cartItem);
        assertEquals("Item added to cart successfully", response.getBody());
        assertEquals(200, response.getStatusCodeValue());

        verify(cartService, times(1)).addItemToCart(eq("userId1234"), eq(cartItem));
    }

    @Test
    public void testRemoveFromCart_ItemRemovedSuccessfully() {
        String userName = "Xyz";
        String userId = "userId1234";
        String productId = "productId";
        User user = new User();
        user.set_id(userId);

        when(authentication.getName()).thenReturn(userName);
        when(userRepo.findByUsername(userName)).thenReturn(Optional.of(user));
        when(cartService.checkIfItemExistsInCart(userId, productId)).thenReturn(true);
        when(cartService.removeItemFromCart(userId, productId)).thenReturn(true);

        ResponseEntity<String> response = cartController.removeFromCart(productId);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Item removed from cart successfully", response.getBody());
    }

    @Test
    public void testRemoveFromCart_ItemNotRemovedSuccessfully() {
        String userName = "Xyz";
        String userId = "userId1234";
        String productId = "productId";
        User user = new User();
        user.set_id(userId);

        when(authentication.getName()).thenReturn(userName);
        when(userRepo.findByUsername(userName)).thenReturn(Optional.of(user));
        when(cartService.checkIfItemExistsInCart(userId, productId)).thenReturn(true);
        when(cartService.removeItemFromCart(userId, productId)).thenReturn(false);

        ResponseEntity<String> response = cartController.removeFromCart(productId);
        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Failed to remove item from cart", response.getBody());
    }

    @Test
    public void testRemoveFromCart_ItemNotFoundInCart() {
        String userName = "Xyz";
        String userId = "userId1234";
        String productId = "productId";
        User user = new User();
        user.set_id(userId);

        when(authentication.getName()).thenReturn(userName);
        when(userRepo.findByUsername(userName)).thenReturn(Optional.of(user));
        when(cartService.checkIfItemExistsInCart(userId, productId)).thenReturn(false);

        ResponseEntity<String> response = cartController.removeFromCart(productId);
        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Item not found in cart.", response.getBody());
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }
}

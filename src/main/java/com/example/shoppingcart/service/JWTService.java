package com.example.shoppingcart.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
public class JWTService {
//    private String secretKey="c7sc";//random
//
//    public JWTService(){
//        try {
//            KeyGenerator keyGenerator=KeyGenerator.getInstance("HmacSHA256");
//            keyGenerator.init(256);
//            SecretKey sk= keyGenerator.generateKey();
//
//            Base64.getEncoder().encodeToString(sk.getEncoded());
//        } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public String generateToken(String userName){
//        Map<String,Object> claims=new HashMap<>();
//        return Jwts.builder()
//                .setClaims(claims)
//                .setSubject(userName)
//                .setIssuedAt(new Date(System.currentTimeMillis()))
//                .setExpiration(new Date(System.currentTimeMillis()*60*60*30))
//                .signWith(getKey())
//                .compact();
//
//    }
//
//
//    public Key getKey() {
//        byte[] keyBytes= Decoders.BASE64.decode(secretKey);
//        return Keys.hmacShaKeyFor(keyBytes);
//    }

    private SecretKey secretKey;

    public JWTService() {
        // Generates a secure random key for HS256
        this.secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }

    public String generateToken(String userName) {
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userName)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 30)) // 30 hours
                .signWith(secretKey)
                .compact();
    }

    public Key getKey() {
        return secretKey;
    }


    public String extractUserName(String token) {
 
      return extractClaim(token,Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims,T> claimResolver) {
        final Claims claims=extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token){
//        return Jwts.parserBuilder()
//                .setSigningKey(getKey())
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey) // Replace with your signing key
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: " + token, e);
            throw new IllegalArgumentException("Invalid JWT token format", e);
        }

    }


    public boolean validateToken(String token, UserDetails userDetails) {
       final String userName=extractUserName(token);
       return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }



}
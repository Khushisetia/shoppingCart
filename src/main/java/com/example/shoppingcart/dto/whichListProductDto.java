package com.example.shoppingcart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class whichListProductDto {
    public String productId;
    public String productName;
     public double price;

    public MultipartFile imageFile;
}

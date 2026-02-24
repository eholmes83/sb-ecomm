package com.echapps.ecom.project.product.service;

import com.echapps.ecom.project.product.dto.request.ProductRequest;
import com.echapps.ecom.project.product.dto.response.ProductResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProductService {
    ProductRequest addProduct(ProductRequest product, Long categoryId);

    ProductResponse getAllProducts();

    ProductRequest deleteProduct(Long productId);

    ProductResponse searchByCategory(Long categoryId);

    ProductResponse searchProductsByKeyword(String keyword);

    ProductRequest updateProduct(ProductRequest product, Long productId);

    ProductRequest updateProductImage(Long productId, MultipartFile image) throws IOException;
}

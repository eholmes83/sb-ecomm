package com.echapps.ecom.project.product.service;

import com.echapps.ecom.project.product.dto.request.ProductRequest;
import com.echapps.ecom.project.product.dto.response.ProductResponse;
import com.echapps.ecom.project.product.model.Product;

public interface ProductService {
    ProductRequest addProduct(Product product, Long categoryId);

    ProductResponse getAllProducts();

    Product deleteProduct(Long productId);

    ProductResponse searchByCategory(Long categoryId);

    ProductResponse searchProductsByKeyword(String keyword);
}

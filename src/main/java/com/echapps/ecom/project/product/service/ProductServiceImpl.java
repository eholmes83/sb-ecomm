package com.echapps.ecom.project.product.service;

import com.echapps.ecom.project.category.model.Category;
import com.echapps.ecom.project.category.repository.CategoryRepository;
import com.echapps.ecom.project.exceptions.ResourceNotFoundException;
import com.echapps.ecom.project.product.dto.request.ProductRequest;
import com.echapps.ecom.project.product.dto.response.ProductResponse;
import com.echapps.ecom.project.product.model.Product;
import com.echapps.ecom.project.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;
    private final Double PERCENTAGE_RATE = 0.01;

    public ProductServiceImpl(CategoryRepository categoryRepository, ProductRepository productRepository, ObjectMapper objectMapper) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public ProductRequest addProduct(Product product, Long categoryId) {
        Category category = categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        product.setImage("default.png");
        product.setCategory(category);
        double specialPrice = product.getPrice() - ((product.getDiscount() * PERCENTAGE_RATE) * product.getPrice());
        product.setSpecialPrice(specialPrice);
        Product savedProduct = productRepository.save(product);
        return objectMapper.convertValue(savedProduct, ProductRequest.class);
    }

    @Override
    public ProductResponse getAllProducts() {
        List<Product> products = productRepository.findAll();
        List<ProductRequest> productRequest = products.stream()
                .map(product -> objectMapper.convertValue(product, ProductRequest.class))
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productRequest);
        return productResponse;
    }

    @Override
    public Product deleteProduct(Long productId) {
        Product productToDelete = productRepository
                .findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        productRepository.deleteById(productId);
        return productToDelete;
    }

    @Override
    public ProductResponse searchByCategory(Long categoryId) {
        Category category = categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        List<Product> products = productRepository.findByCategoryOrderByPriceAsc(category);
        List<ProductRequest> productRequest = products.stream()
                .map(product -> objectMapper.convertValue(product, ProductRequest.class))
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productRequest);
        return productResponse;

    }

    @Override
    public ProductResponse searchProductsByKeyword(String keyword) {
        List<Product> products = productRepository.findByProductNameLikeIgnoreCase('%' + keyword + '%');
        List<ProductRequest> productRequest = products.stream()
                .map(product -> objectMapper.convertValue(product, ProductRequest.class))
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productRequest);
        return productResponse;
    }

}

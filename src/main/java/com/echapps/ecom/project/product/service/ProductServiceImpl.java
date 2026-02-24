package com.echapps.ecom.project.product.service;

import com.echapps.ecom.project.category.model.Category;
import com.echapps.ecom.project.category.repository.CategoryRepository;
import com.echapps.ecom.project.exceptions.ResourceNotFoundException;
import com.echapps.ecom.project.product.dto.request.ProductRequest;
import com.echapps.ecom.project.product.dto.response.ProductResponse;
import com.echapps.ecom.project.product.model.Product;
import com.echapps.ecom.project.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final FileService fileService;
    private final ObjectMapper objectMapper;

    @Value("${project.image}")
    private String path;

    public ProductServiceImpl(CategoryRepository categoryRepository, ProductRepository productRepository, FileService fileService, ObjectMapper objectMapper) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.fileService = fileService;
        this.objectMapper = objectMapper;
    }

    @Override
    public ProductRequest addProduct(ProductRequest productRequest, Long categoryId) {
        Category category = categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        Product product = objectMapper.convertValue(productRequest, Product.class);
        product.setImage("default.png");
        product.setCategory(category);
        double specialPrice = calculateSpecialPrice(product.getPrice(), product.getDiscount());
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
    public ProductRequest deleteProduct(Long productId) {
        Product productToDelete = productRepository
                .findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        productRepository.deleteById(productId);
        return objectMapper.convertValue(productToDelete, ProductRequest.class);
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

    @Override
    public ProductRequest updateProduct(ProductRequest productRequest, Long productId) {
        Product productToUpdate = productRepository
                .findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        Product product = objectMapper.convertValue(productRequest, Product.class);
        productToUpdate.setProductName(product.getProductName());
        productToUpdate.setDescription(product.getDescription());
        productToUpdate.setQuantity(product.getQuantity());
        productToUpdate.setPrice(product.getPrice());
        productToUpdate.setDiscount(product.getDiscount());
        productToUpdate.setSpecialPrice(calculateSpecialPrice(product.getPrice(), product.getDiscount()));

        Product updatedProduct = productRepository.save(productToUpdate);
        return objectMapper.convertValue(updatedProduct, ProductRequest.class);
    }

    @Override
    public ProductRequest updateProductImage(Long productId, MultipartFile image) throws IOException {
        Product productToUpdate = productRepository
                .findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        String fileName = fileService.uploadImage(path, image);
        productToUpdate.setImage(fileName);

        Product updatedProduct = productRepository.save(productToUpdate);
        return objectMapper.convertValue(updatedProduct, ProductRequest.class);
    }

    private double calculateSpecialPrice(Double price, Double discount) {
        Double PERCENTAGE_RATE = 0.01;
        return price - ((discount * PERCENTAGE_RATE) * price);
    }

}

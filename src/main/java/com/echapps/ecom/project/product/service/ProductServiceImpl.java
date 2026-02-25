package com.echapps.ecom.project.product.service;

import com.echapps.ecom.project.category.model.Category;
import com.echapps.ecom.project.category.repository.CategoryRepository;
import com.echapps.ecom.project.exceptions.APIException;
import com.echapps.ecom.project.exceptions.ResourceNotFoundException;
import com.echapps.ecom.project.product.dto.request.ProductRequest;
import com.echapps.ecom.project.product.dto.response.ProductResponse;
import com.echapps.ecom.project.product.model.Product;
import com.echapps.ecom.project.product.repository.ProductRepository;
import com.echapps.ecom.project.product.service.file.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

        boolean isProductPresent = false;

        List<Product> products = category.getProducts();
        for (Product product : products) {
            if (product.getProductName().equalsIgnoreCase(productRequest.getProductName())) {
                isProductPresent = true;
                break;
            }
        }

        if (!isProductPresent) {
            Product product = objectMapper.convertValue(productRequest, Product.class);
            product.setImage("default.png");
            product.setCategory(category);
            double specialPrice = calculateSpecialPrice(product.getPrice(), product.getDiscount());
            product.setSpecialPrice(specialPrice);
            Product savedProduct = productRepository.save(product);
            return objectMapper.convertValue(savedProduct, ProductRequest.class);
        } else {
            throw new APIException("Product with name " + productRequest.getProductName() + " already exists in category " + category.getCategoryName());
        }
    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> productPage = productRepository.findAll(pageDetails);
        List<Product> products = productPage.getContent();

        List<ProductRequest> productRequest = products.stream()
                .map(product -> objectMapper.convertValue(product, ProductRequest.class))
                .toList();

        if (products.isEmpty()) {
            throw new APIException("No products found!");
        }

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productRequest);
        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPageSize(productPage.getSize());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setLastPage(productPage.isLast());
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
    public ProductResponse searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Category category = categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> productPage = productRepository.findByCategoryOrderByPriceAsc(category, pageDetails);
        List<Product> products = productPage.getContent();

        if (products.isEmpty()) {
            throw new APIException("No products found in " + category.getCategoryName() + " category!");
        }

        List<ProductRequest> productRequest = products.stream()
                .map(product -> objectMapper.convertValue(product, ProductRequest.class))
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productRequest);
        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPageSize(productPage.getSize());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setLastPage(productPage.isLast());
        return productResponse;

    }

    @Override
    public ProductResponse searchProductsByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> productPage = productRepository.findByProductNameLikeIgnoreCase('%' + keyword + '%', pageDetails);
        List<Product> products = productPage.getContent();

        List<ProductRequest> productRequest = products.stream()
                .map(product -> objectMapper.convertValue(product, ProductRequest.class))
                .toList();

        if (products.isEmpty()) {
            throw new APIException("No products found with keyword: " + keyword);
        }

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productRequest);
        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPageSize(productPage.getSize());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setLastPage(productPage.isLast());
        return productResponse;
    }

    @Override
    public ProductRequest updateProduct(ProductRequest productRequest, Long productId) {
        Product productToUpdate = productRepository
                .findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));


        if (productToUpdate.getProductName().equalsIgnoreCase(productRequest.getProductName())) {
            throw new APIException("Product with name " + productRequest.getProductName() + " already exists!");
        }

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

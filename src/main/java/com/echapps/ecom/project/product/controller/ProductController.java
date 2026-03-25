package com.echapps.ecom.project.product.controller;


import com.echapps.ecom.project.config.AppConstants;
import com.echapps.ecom.project.product.dto.request.ProductRequest;
import com.echapps.ecom.project.product.dto.response.ProductResponse;
import com.echapps.ecom.project.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1")
public class ProductController {

    ProductService productService;

    ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/admin/categories/{categoryId}/product")
    @Operation(summary = "Add a new product to a category", description = "Create a new product under a specific category by providing the product details in the request body and the category ID in the path variable.")
    @Tag(name = "Product APIs", description = "APIs for managing products")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid product data", content = @Content),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<ProductRequest> addProduct(@Valid @RequestBody ProductRequest productRequest,
                                                     @Parameter(description = "Id of product to add to cart") @PathVariable Long categoryId) {
       ProductRequest productToAdd = productService.addProduct(productRequest, categoryId);
       return new ResponseEntity<>(productToAdd, HttpStatus.CREATED);
    }

    @GetMapping("/public/products")
    @Operation(summary = "Get all products with pagination and sorting", description = "Retrieve a paginated and sorted list of all products. You can specify the page number, page size, sorting field, and sorting order.")
    @Tag(name = "Product APIs", description = "APIs for managing products")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved products"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<ProductResponse> getAllProducts(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER_0, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE_50, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_BY_PRODUCT_ID, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.ASC_SORT_DIRECTION, required = false) String sortOrder) {

        ProductResponse productResponse = productService.getAllProducts(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);

    }

    @GetMapping("/public/categories/{categoryId}/products")
    @Operation(summary = "Get products by category with pagination and sorting", description = "Retrieve a paginated and sorted list of products under a specific category. You can specify the page number, page size, sorting field, and sorting order.")
    @Tag(name = "Product APIs", description = "APIs for managing products")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved products for the category"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<ProductResponse> getProductsByCategory(
            @Parameter(description = "Id of category to get products from") @PathVariable Long categoryId,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER_0, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE_50, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_BY_PRODUCT_ID, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.ASC_SORT_DIRECTION, required = false) String sortOrder) {

        ProductResponse productResponse = productService.searchByCategory(categoryId, pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @GetMapping("/public/products/keyword/{keyword}")
    @Operation(summary = "Search products by keyword with pagination and sorting", description = "Search for products based on a keyword in their name or description. You can specify the page number, page size, sorting field, and sorting order.")
    @Tag(name = "Product APIs", description = "APIs for managing products")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products found matching the keyword"),
            @ApiResponse(responseCode = "404", description = "No products found matching the keyword", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<ProductResponse> searchProductsByKeyword(
            @Parameter(description = "Keyword to search for products") @PathVariable String keyword,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER_0, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE_50, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_BY_PRODUCT_ID, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.ASC_SORT_DIRECTION, required = false) String sortOrder) {
        ProductResponse productResponse = productService.searchProductsByKeyword(keyword, pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(productResponse, HttpStatus.FOUND);
    }

    @PutMapping("/admin/products/{productId}")
    @Operation(summary = "Update a product by ID", description = "Update the details of an existing product by providing its ID in the path variable and the updated product details in the request body.")
    @Tag(name = "Product APIs", description = "APIs for managing products")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid product data", content = @Content),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<ProductRequest> updateProduct(@Valid @RequestBody ProductRequest productRequest,
                                                        @Parameter(description = "Id of product to update") @PathVariable Long productId) {
        ProductRequest productToUpdate = productService.updateProduct(productRequest, productId);
        return new ResponseEntity<>(productToUpdate, HttpStatus.OK);
    }

    @DeleteMapping("/admin/products/{productId}")
    @Operation(summary = "Delete a product by ID", description = "Delete an existing product by providing its ID in the path variable.")
    @Tag(name = "Product APIs", description = "APIs for managing products")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<ProductRequest> deleteProduct(@Parameter(description = "Id of product to delete") @PathVariable Long productId) {
        ProductRequest productToDelete = productService.deleteProduct(productId);
        return new ResponseEntity<>(productToDelete, HttpStatus.OK);
    }

    @PutMapping("/admin/products/{productId}/image")
    @Operation(summary = "Update product image", description = "Update the image of an existing product by providing its ID in the path variable and the new image file in the request parameter.")
    @Tag(name = "Product APIs", description = "APIs for managing products")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product image updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid image file", content = @Content),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<ProductRequest> updateProductImage(@Parameter(description = "Id of product to update image") @PathVariable Long productId,
                                                             @RequestParam("image") MultipartFile image) throws IOException {
        ProductRequest updatedProduct = productService.updateProductImage(productId, image);
        return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
    }
}

package com.echapps.ecom.project.product.service;

import com.echapps.ecom.project.cart.dto.request.CartDTO;
import com.echapps.ecom.project.cart.model.Cart;
import com.echapps.ecom.project.cart.model.CartItem;
import com.echapps.ecom.project.cart.repository.CartRepository;
import com.echapps.ecom.project.cart.service.CartService;
import com.echapps.ecom.project.category.model.Category;
import com.echapps.ecom.project.category.repository.CategoryRepository;
import com.echapps.ecom.project.exceptions.APIException;
import com.echapps.ecom.project.exceptions.ResourceNotFoundException;
import com.echapps.ecom.project.product.dto.request.ProductRequest;
import com.echapps.ecom.project.product.dto.response.ProductResponse;
import com.echapps.ecom.project.product.model.Product;
import com.echapps.ecom.project.product.repository.ProductRepository;
import com.echapps.ecom.project.product.service.file.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductServiceImpl layer.
 *
 * Testing Framework Choice: JUnit 5 (Jupiter) + Mockito
 * Rationale:
 * - JUnit 5 provides modern testing features with better parameterization and nested tests
 * - Mockito isolates the service from database/repository and file system dependencies
 * - ObjectMapper is mocked to test pure business logic without JSON serialization concerns
 * - Tests follow the vertical slice architecture by residing in src/test/java/com/echapps/ecom/project/product/service/
 * - Nested test classes organize tests by method, improving readability and maintainability
 * - ReflectionTestUtils injects the @Value("${project.image}") path field without a Spring context
 *
 * Test Coverage:
 * - Happy paths: successful operations with valid inputs
 * - Edge cases: empty results, null values, boundary conditions
 * - Error cases: exceptions thrown by repository, duplicate checks, not-found scenarios
 * - Special price calculation: verified through the product saved to the repository
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductServiceImpl Unit Tests")
class ProductServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private FileService fileService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartService cartService;

    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(
                categoryRepository,
                productRepository,
                fileService,
                objectMapper,
                cartRepository,
                cartService
        );
        ReflectionTestUtils.setField(productService, "path", "images/");
    }

    // ---------------------------------------------------------------------------
    // Helper builders
    // ---------------------------------------------------------------------------

    private Product buildProduct(Long id, String name, Double price, Double discount, Category category) {
        Product p = new Product();
        p.setProductId(id);
        p.setProductName(name);
        p.setImage("default.png");
        p.setDescription("A product description");
        p.setQuantity(10);
        p.setPrice(price);
        p.setDiscount(discount);
        p.setSpecialPrice(price - (discount * 0.01 * price));
        p.setCategory(category);
        return p;
    }

    private ProductRequest buildProductRequest(Long id, String name, Double price, Double discount, Double specialPrice) {
        return new ProductRequest(id, name, "default.png", "A product description", 10, price, discount, specialPrice);
    }

    private Category buildCategory(Long id, String name) {
        return new Category(id, name, new ArrayList<>());
    }

    // ===========================================================================
    // addProduct
    // ===========================================================================

    @Nested
    @DisplayName("addProduct")
    class AddProductTests {

        @Test
        @DisplayName("Should successfully add a new product to a category")
        void shouldSuccessfullyAddNewProduct() {
            // Arrange
            Long categoryId = 1L;
            Category category = buildCategory(categoryId, "Electronics");

            ProductRequest request = buildProductRequest(null, "Laptop", 1000.0, 10.0, null);
            Product productToSave = buildProduct(null, "Laptop", 1000.0, 10.0, null);
            Product savedProduct = buildProduct(1L, "Laptop", 1000.0, 10.0, category);
            savedProduct.setSpecialPrice(900.0);
            ProductRequest expectedResponse = buildProductRequest(1L, "Laptop", 1000.0, 10.0, 900.0);

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
            when(objectMapper.convertValue(request, Product.class)).thenReturn(productToSave);
            when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
            when(objectMapper.convertValue(savedProduct, ProductRequest.class)).thenReturn(expectedResponse);

            // Act
            ProductRequest result = productService.addProduct(request, categoryId);

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getProductId());
            assertEquals("Laptop", result.getProductName());
            assertEquals(900.0, result.getSpecialPrice());
            verify(categoryRepository).findById(categoryId);
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when category does not exist")
        void shouldThrowResourceNotFoundExceptionWhenCategoryNotFound() {
            // Arrange
            Long categoryId = 999L;
            ProductRequest request = buildProductRequest(null, "Laptop", 1000.0, 10.0, null);

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                    productService.addProduct(request, categoryId));
            assertEquals("Category not found with categoryId : 999", exception.getMessage());
            verify(categoryRepository).findById(categoryId);
            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        @DisplayName("Should throw APIException when product already exists in category (case-insensitive)")
        void shouldThrowAPIExceptionWhenProductAlreadyExistsInCategory() {
            // Arrange
            Long categoryId = 1L;
            Product existingProduct = buildProduct(1L, "Laptop", 1000.0, 10.0, null);
            Category category = new Category(categoryId, "Electronics", List.of(existingProduct));

            ProductRequest request = buildProductRequest(null, "laptop", 1000.0, 10.0, null); // lowercase duplicate

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

            // Act & Assert
            APIException exception = assertThrows(APIException.class, () ->
                    productService.addProduct(request, categoryId));
            assertTrue(exception.getMessage().contains("laptop"));
            assertTrue(exception.getMessage().contains("already exists"));
            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        @DisplayName("Should set default image and calculate special price when adding a product")
        void shouldSetDefaultImageAndCalculateSpecialPrice() {
            // Arrange
            Long categoryId = 1L;
            Category category = buildCategory(categoryId, "Electronics");

            ProductRequest request = buildProductRequest(null, "Phone", 500.0, 20.0, null);
            Product productToSave = new Product();
            productToSave.setProductName("Phone");
            productToSave.setPrice(500.0);
            productToSave.setDiscount(20.0);
            // specialPrice: 500 - (20 * 0.01 * 500) = 500 - 100 = 400
            Product savedProduct = buildProduct(2L, "Phone", 500.0, 20.0, category);
            savedProduct.setSpecialPrice(400.0);
            ProductRequest expectedResponse = buildProductRequest(2L, "Phone", 500.0, 20.0, 400.0);

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
            when(objectMapper.convertValue(request, Product.class)).thenReturn(productToSave);
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
                Product saved = invocation.getArgument(0);
                assertEquals("default.png", saved.getImage());
                assertEquals(400.0, saved.getSpecialPrice());
                return savedProduct;
            });
            when(objectMapper.convertValue(savedProduct, ProductRequest.class)).thenReturn(expectedResponse);

            // Act
            ProductRequest result = productService.addProduct(request, categoryId);

            // Assert
            assertNotNull(result);
            assertEquals(400.0, result.getSpecialPrice());
        }

        @Test
        @DisplayName("Should successfully add product when category has other products with different names")
        void shouldAddProductWhenCategoryHasOtherProducts() {
            // Arrange
            Long categoryId = 1L;
            Product existingProduct = buildProduct(1L, "Tablet", 300.0, 5.0, null);
            Category category = new Category(categoryId, "Electronics", new ArrayList<>(List.of(existingProduct)));

            ProductRequest request = buildProductRequest(null, "Laptop", 1000.0, 10.0, null);
            Product productToSave = buildProduct(null, "Laptop", 1000.0, 10.0, null);
            Product savedProduct = buildProduct(2L, "Laptop", 1000.0, 10.0, category);
            ProductRequest expectedResponse = buildProductRequest(2L, "Laptop", 1000.0, 10.0, 900.0);

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
            when(objectMapper.convertValue(request, Product.class)).thenReturn(productToSave);
            when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
            when(objectMapper.convertValue(savedProduct, ProductRequest.class)).thenReturn(expectedResponse);

            // Act
            ProductRequest result = productService.addProduct(request, categoryId);

            // Assert
            assertNotNull(result);
            assertEquals(2L, result.getProductId());
            verify(productRepository).save(any(Product.class));
        }
    }

    // ===========================================================================
    // getAllProducts
    // ===========================================================================

    @Nested
    @DisplayName("getAllProducts")
    class GetAllProductsTests {

        @Test
        @DisplayName("Should return paginated products with ascending sort order")
        void shouldReturnPaginatedProductsWithAscendingSort() {
            // Arrange
            Integer pageNumber = 0;
            Integer pageSize = 10;
            String sortBy = "productId";
            String sortOrder = "asc";

            Category category = buildCategory(1L, "Electronics");
            Product product1 = buildProduct(1L, "Laptop", 1000.0, 10.0, category);
            Product product2 = buildProduct(2L, "Phone", 500.0, 5.0, category);
            List<Product> products = List.of(product1, product2);

            Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).ascending());
            Page<Product> productPage = new PageImpl<>(products, pageable, 2);

            ProductRequest pr1 = buildProductRequest(1L, "Laptop", 1000.0, 10.0, 900.0);
            ProductRequest pr2 = buildProductRequest(2L, "Phone", 500.0, 5.0, 475.0);

            when(productRepository.findAll(any(Pageable.class))).thenReturn(productPage);
            when(objectMapper.convertValue(product1, ProductRequest.class)).thenReturn(pr1);
            when(objectMapper.convertValue(product2, ProductRequest.class)).thenReturn(pr2);

            // Act
            ProductResponse response = productService.getAllProducts(pageNumber, pageSize, sortBy, sortOrder);

            // Assert
            assertNotNull(response);
            assertEquals(2, response.getContent().size());
            assertEquals(0, response.getPageNumber());
            assertEquals(10, response.getPageSize());
            assertEquals(2L, response.getTotalElements());
            assertEquals(1, response.getTotalPages());
            assertTrue(response.getLastPage());
            verify(productRepository).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("Should return paginated products with descending sort order")
        void shouldReturnPaginatedProductsWithDescendingSort() {
            // Arrange
            Integer pageNumber = 0;
            Integer pageSize = 10;
            String sortBy = "price";
            String sortOrder = "desc";

            Category category = buildCategory(1L, "Electronics");
            Product product = buildProduct(1L, "Laptop", 1000.0, 10.0, category);
            List<Product> products = List.of(product);

            Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).descending());
            Page<Product> productPage = new PageImpl<>(products, pageable, 1);

            ProductRequest pr = buildProductRequest(1L, "Laptop", 1000.0, 10.0, 900.0);

            when(productRepository.findAll(any(Pageable.class))).thenReturn(productPage);
            when(objectMapper.convertValue(product, ProductRequest.class)).thenReturn(pr);

            // Act
            ProductResponse response = productService.getAllProducts(pageNumber, pageSize, sortBy, sortOrder);

            // Assert
            assertNotNull(response);
            assertEquals(1, response.getContent().size());
            assertEquals("Laptop", response.getContent().get(0).getProductName());
        }

        @Test
        @DisplayName("Should throw APIException when no products are found")
        void shouldThrowAPIExceptionWhenNoProductsFound() {
            // Arrange
            Integer pageNumber = 0;
            Integer pageSize = 10;
            String sortBy = "productId";
            String sortOrder = "asc";

            Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).ascending());
            Page<Product> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(productRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

            // Act & Assert
            APIException exception = assertThrows(APIException.class, () ->
                    productService.getAllProducts(pageNumber, pageSize, sortBy, sortOrder));
            assertEquals("No products found!", exception.getMessage());
            verify(productRepository).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle multiple pages correctly")
        void shouldHandleMultiplePagesCorrectly() {
            // Arrange
            Integer pageNumber = 1;
            Integer pageSize = 2;
            String sortBy = "productId";
            String sortOrder = "asc";

            Category category = buildCategory(1L, "Electronics");
            Product product3 = buildProduct(3L, "Headphones", 150.0, 0.0, category);
            Product product4 = buildProduct(4L, "Keyboard", 80.0, 5.0, category);
            List<Product> products = List.of(product3, product4);

            Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).ascending());
            Page<Product> productPage = new PageImpl<>(products, pageable, 5);

            ProductRequest pr3 = buildProductRequest(3L, "Headphones", 150.0, 0.0, 150.0);
            ProductRequest pr4 = buildProductRequest(4L, "Keyboard", 80.0, 5.0, 76.0);

            when(productRepository.findAll(any(Pageable.class))).thenReturn(productPage);
            when(objectMapper.convertValue(product3, ProductRequest.class)).thenReturn(pr3);
            when(objectMapper.convertValue(product4, ProductRequest.class)).thenReturn(pr4);

            // Act
            ProductResponse response = productService.getAllProducts(pageNumber, pageSize, sortBy, sortOrder);

            // Assert
            assertEquals(1, response.getPageNumber());
            assertEquals(2, response.getPageSize());
            assertEquals(5L, response.getTotalElements());
            assertEquals(3, response.getTotalPages());
            assertFalse(response.getLastPage());
        }
    }

    // ===========================================================================
    // deleteProduct
    // ===========================================================================

    @Nested
    @DisplayName("deleteProduct")
    class DeleteProductTests {

        @Test
        @DisplayName("Should successfully delete a product")
        void shouldSuccessfullyDeleteProduct() {
            // Arrange
            Long productId = 1L;
            Category category = buildCategory(1L, "Electronics");
            Product product = buildProduct(productId, "Laptop", 1000.0, 10.0, category);
            ProductRequest expectedResponse = buildProductRequest(productId, "Laptop", 1000.0, 10.0, 900.0);

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(cartRepository.findCartsByProductId(productId)).thenReturn(Collections.emptyList());
            when(objectMapper.convertValue(product, ProductRequest.class)).thenReturn(expectedResponse);

            // Act
            ProductRequest result = productService.deleteProduct(productId);

            // Assert
            assertNotNull(result);
            assertEquals(productId, result.getProductId());
            assertEquals("Laptop", result.getProductName());
            verify(productRepository).findById(productId);
            verify(productRepository).deleteById(productId);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when product does not exist")
        void shouldThrowResourceNotFoundExceptionWhenProductNotFound() {
            // Arrange
            Long productId = 999L;
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                    productService.deleteProduct(productId));
            assertEquals("Product not found with productId : 999", exception.getMessage());
            verify(productRepository).findById(productId);
            verify(productRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Should remove product from all carts before deleting")
        void shouldRemoveProductFromAllCartsBeforeDeleting() {
            // Arrange
            Long productId = 1L;
            Category category = buildCategory(1L, "Electronics");
            Product product = buildProduct(productId, "Laptop", 1000.0, 10.0, category);
            ProductRequest expectedResponse = buildProductRequest(productId, "Laptop", 1000.0, 10.0, 900.0);

            Cart cart1 = new Cart(10L, null, new ArrayList<>(), 900.0);
            Cart cart2 = new Cart(20L, null, new ArrayList<>(), 1800.0);
            List<Cart> carts = List.of(cart1, cart2);

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(cartRepository.findCartsByProductId(productId)).thenReturn(carts);
            when(objectMapper.convertValue(product, ProductRequest.class)).thenReturn(expectedResponse);

            // Act
            productService.deleteProduct(productId);

            // Assert
            verify(cartService).deleteProductFromCart(10L, productId);
            verify(cartService).deleteProductFromCart(20L, productId);
            verify(productRepository).deleteById(productId);
        }

        @Test
        @DisplayName("Should still delete product when it belongs to no carts")
        void shouldDeleteProductNotInAnyCart() {
            // Arrange
            Long productId = 5L;
            Category category = buildCategory(2L, "Books");
            Product product = buildProduct(productId, "Java Guide", 50.0, 0.0, category);
            ProductRequest expectedResponse = buildProductRequest(productId, "Java Guide", 50.0, 0.0, 50.0);

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(cartRepository.findCartsByProductId(productId)).thenReturn(Collections.emptyList());
            when(objectMapper.convertValue(product, ProductRequest.class)).thenReturn(expectedResponse);

            // Act
            productService.deleteProduct(productId);

            // Assert
            verify(cartService, never()).deleteProductFromCart(any(), any());
            verify(productRepository).deleteById(productId);
        }
    }

    // ===========================================================================
    // searchByCategory
    // ===========================================================================

    @Nested
    @DisplayName("searchByCategory")
    class SearchByCategoryTests {

        @Test
        @DisplayName("Should return paginated products filtered by category")
        void shouldReturnProductsFilteredByCategory() {
            // Arrange
            Long categoryId = 1L;
            Integer pageNumber = 0;
            Integer pageSize = 10;
            String sortBy = "price";
            String sortOrder = "asc";

            Category category = buildCategory(categoryId, "Electronics");
            Product product = buildProduct(1L, "Laptop", 1000.0, 10.0, category);
            List<Product> products = List.of(product);

            Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).ascending());
            Page<Product> productPage = new PageImpl<>(products, pageable, 1);
            ProductRequest pr = buildProductRequest(1L, "Laptop", 1000.0, 10.0, 900.0);

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
            when(productRepository.findByCategoryOrderByPriceAsc(eq(category), any(Pageable.class))).thenReturn(productPage);
            when(objectMapper.convertValue(product, ProductRequest.class)).thenReturn(pr);

            // Act
            ProductResponse response = productService.searchByCategory(categoryId, pageNumber, pageSize, sortBy, sortOrder);

            // Assert
            assertNotNull(response);
            assertEquals(1, response.getContent().size());
            assertEquals("Laptop", response.getContent().get(0).getProductName());
            verify(categoryRepository).findById(categoryId);
            verify(productRepository).findByCategoryOrderByPriceAsc(eq(category), any(Pageable.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when category does not exist")
        void shouldThrowResourceNotFoundExceptionWhenCategoryNotFound() {
            // Arrange
            Long categoryId = 999L;
            when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                    productService.searchByCategory(categoryId, 0, 10, "price", "asc"));
            assertEquals("Category not found with categoryId : 999", exception.getMessage());
            verify(productRepository, never()).findByCategoryOrderByPriceAsc(any(), any());
        }

        @Test
        @DisplayName("Should throw APIException when no products exist in the given category")
        void shouldThrowAPIExceptionWhenNoCategoryProductsFound() {
            // Arrange
            Long categoryId = 1L;
            Category category = buildCategory(categoryId, "Toys");

            Pageable pageable = PageRequest.of(0, 10, Sort.by("price").ascending());
            Page<Product> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
            when(productRepository.findByCategoryOrderByPriceAsc(eq(category), any(Pageable.class))).thenReturn(emptyPage);

            // Act & Assert
            APIException exception = assertThrows(APIException.class, () ->
                    productService.searchByCategory(categoryId, 0, 10, "price", "asc"));
            assertEquals("No products found in Toys category!", exception.getMessage());
        }

        @Test
        @DisplayName("Should return products with descending sort when sortOrder is desc")
        void shouldReturnProductsWithDescendingSort() {
            // Arrange
            Long categoryId = 1L;
            Category category = buildCategory(categoryId, "Electronics");
            Product product = buildProduct(1L, "Laptop", 1000.0, 10.0, category);
            List<Product> products = List.of(product);

            Pageable pageable = PageRequest.of(0, 10, Sort.by("price").descending());
            Page<Product> productPage = new PageImpl<>(products, pageable, 1);
            ProductRequest pr = buildProductRequest(1L, "Laptop", 1000.0, 10.0, 900.0);

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
            when(productRepository.findByCategoryOrderByPriceAsc(eq(category), any(Pageable.class))).thenReturn(productPage);
            when(objectMapper.convertValue(product, ProductRequest.class)).thenReturn(pr);

            // Act
            ProductResponse response = productService.searchByCategory(categoryId, 0, 10, "price", "desc");

            // Assert
            assertNotNull(response);
            assertEquals(1, response.getContent().size());
        }
    }

    // ===========================================================================
    // searchProductsByKeyword
    // ===========================================================================

    @Nested
    @DisplayName("searchProductsByKeyword")
    class SearchProductsByKeywordTests {

        @Test
        @DisplayName("Should return products matching the given keyword")
        void shouldReturnProductsMatchingKeyword() {
            // Arrange
            String keyword = "Laptop";
            Integer pageNumber = 0;
            Integer pageSize = 10;
            String sortBy = "productName";
            String sortOrder = "asc";

            Category category = buildCategory(1L, "Electronics");
            Product product = buildProduct(1L, "Laptop Pro", 1200.0, 15.0, category);
            List<Product> products = List.of(product);

            Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).ascending());
            Page<Product> productPage = new PageImpl<>(products, pageable, 1);
            ProductRequest pr = buildProductRequest(1L, "Laptop Pro", 1200.0, 15.0, 1020.0);

            when(productRepository.findByProductNameLikeIgnoreCase(eq("%Laptop%"), any(Pageable.class))).thenReturn(productPage);
            when(objectMapper.convertValue(product, ProductRequest.class)).thenReturn(pr);

            // Act
            ProductResponse response = productService.searchProductsByKeyword(keyword, pageNumber, pageSize, sortBy, sortOrder);

            // Assert
            assertNotNull(response);
            assertEquals(1, response.getContent().size());
            assertEquals("Laptop Pro", response.getContent().get(0).getProductName());
            verify(productRepository).findByProductNameLikeIgnoreCase(eq("%Laptop%"), any(Pageable.class));
        }

        @Test
        @DisplayName("Should throw APIException when no products match the keyword")
        void shouldThrowAPIExceptionWhenNoProductsMatchKeyword() {
            // Arrange
            String keyword = "xyznotfound";

            Pageable pageable = PageRequest.of(0, 10, Sort.by("productName").ascending());
            Page<Product> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(productRepository.findByProductNameLikeIgnoreCase(eq("%xyznotfound%"), any(Pageable.class))).thenReturn(emptyPage);

            // Act & Assert
            APIException exception = assertThrows(APIException.class, () ->
                    productService.searchProductsByKeyword(keyword, 0, 10, "productName", "asc"));
            assertEquals("No products found with keyword: xyznotfound", exception.getMessage());
        }

        @Test
        @DisplayName("Should wrap keyword with wildcard characters for LIKE query")
        void shouldWrapKeywordWithWildcards() {
            // Arrange
            String keyword = "phone";
            Category category = buildCategory(1L, "Electronics");
            Product product = buildProduct(1L, "Smartphone", 600.0, 10.0, category);
            List<Product> products = List.of(product);

            Pageable pageable = PageRequest.of(0, 10, Sort.by("productId").ascending());
            Page<Product> productPage = new PageImpl<>(products, pageable, 1);
            ProductRequest pr = buildProductRequest(1L, "Smartphone", 600.0, 10.0, 540.0);

            when(productRepository.findByProductNameLikeIgnoreCase(eq("%phone%"), any(Pageable.class))).thenReturn(productPage);
            when(objectMapper.convertValue(product, ProductRequest.class)).thenReturn(pr);

            // Act
            productService.searchProductsByKeyword(keyword, 0, 10, "productId", "asc");

            // Assert
            verify(productRepository).findByProductNameLikeIgnoreCase(eq("%phone%"), any(Pageable.class));
        }

        @Test
        @DisplayName("Should return results sorted in descending order when sortOrder is desc")
        void shouldReturnResultsWithDescendingSort() {
            // Arrange
            String keyword = "pro";
            Category category = buildCategory(1L, "Electronics");
            Product product1 = buildProduct(1L, "Laptop Pro", 1200.0, 10.0, category);
            Product product2 = buildProduct(2L, "Phone Pro", 800.0, 5.0, category);
            List<Product> products = List.of(product1, product2);

            Pageable pageable = PageRequest.of(0, 10, Sort.by("price").descending());
            Page<Product> productPage = new PageImpl<>(products, pageable, 2);
            ProductRequest pr1 = buildProductRequest(1L, "Laptop Pro", 1200.0, 10.0, 1080.0);
            ProductRequest pr2 = buildProductRequest(2L, "Phone Pro", 800.0, 5.0, 760.0);

            when(productRepository.findByProductNameLikeIgnoreCase(eq("%pro%"), any(Pageable.class))).thenReturn(productPage);
            when(objectMapper.convertValue(product1, ProductRequest.class)).thenReturn(pr1);
            when(objectMapper.convertValue(product2, ProductRequest.class)).thenReturn(pr2);

            // Act
            ProductResponse response = productService.searchProductsByKeyword("pro", 0, 10, "price", "desc");

            // Assert
            assertNotNull(response);
            assertEquals(2, response.getContent().size());
        }
    }

    // ===========================================================================
    // updateProduct
    // ===========================================================================

    @Nested
    @DisplayName("updateProduct")
    class UpdateProductTests {

        @Test
        @DisplayName("Should successfully update a product")
        void shouldSuccessfullyUpdateProduct() {
            // Arrange
            Long productId = 1L;
            Category category = buildCategory(1L, "Electronics");
            Product existingProduct = buildProduct(productId, "Laptop", 1000.0, 10.0, category);

            ProductRequest updateRequest = buildProductRequest(productId, "Laptop V2", 1100.0, 15.0, null);
            Product mappedProduct = new Product();
            mappedProduct.setProductName("Laptop V2");
            mappedProduct.setDescription("A product description");
            mappedProduct.setQuantity(10);
            mappedProduct.setPrice(1100.0);
            mappedProduct.setDiscount(15.0);

            Product updatedProduct = buildProduct(productId, "Laptop V2", 1100.0, 15.0, category);
            updatedProduct.setSpecialPrice(935.0);
            ProductRequest expectedResponse = buildProductRequest(productId, "Laptop V2", 1100.0, 15.0, 935.0);

            when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
            when(objectMapper.convertValue(updateRequest, Product.class)).thenReturn(mappedProduct);
            when(productRepository.save(existingProduct)).thenReturn(updatedProduct);
            when(cartRepository.findCartsByProductId(productId)).thenReturn(Collections.emptyList());
            when(objectMapper.convertValue(updatedProduct, ProductRequest.class)).thenReturn(expectedResponse);

            // Act
            ProductRequest result = productService.updateProduct(updateRequest, productId);

            // Assert
            assertNotNull(result);
            assertEquals("Laptop V2", result.getProductName());
            assertEquals(935.0, result.getSpecialPrice());
            verify(productRepository).findById(productId);
            verify(productRepository).save(existingProduct);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when product does not exist")
        void shouldThrowResourceNotFoundExceptionWhenProductNotFound() {
            // Arrange
            Long productId = 999L;
            ProductRequest updateRequest = buildProductRequest(999L, "Non-existent", 100.0, 0.0, null);

            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                    productService.updateProduct(updateRequest, productId));
            assertEquals("Product not found with productId : 999", exception.getMessage());
            verify(productRepository).findById(productId);
            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        @DisplayName("Should throw APIException when new product name matches the existing name (case-insensitive)")
        void shouldThrowAPIExceptionWhenNewNameMatchesExistingName() {
            // Arrange
            Long productId = 1L;
            Category category = buildCategory(1L, "Electronics");
            Product existingProduct = buildProduct(productId, "Laptop", 1000.0, 10.0, category);

            ProductRequest updateRequest = buildProductRequest(productId, "LAPTOP", 1100.0, 10.0, null); // same name, different case

            when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));

            // Act & Assert
            APIException exception = assertThrows(APIException.class, () ->
                    productService.updateProduct(updateRequest, productId));
            assertTrue(exception.getMessage().contains("LAPTOP"));
            assertTrue(exception.getMessage().contains("already exists"));
            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        @DisplayName("Should update all carts containing the product after updating")
        void shouldUpdateCartsAfterProductUpdate() {
            // Arrange
            Long productId = 1L;
            Category category = buildCategory(1L, "Electronics");
            Product existingProduct = buildProduct(productId, "Laptop", 1000.0, 10.0, category);

            ProductRequest updateRequest = buildProductRequest(productId, "Laptop Pro", 1200.0, 10.0, null);
            Product mappedProduct = new Product();
            mappedProduct.setProductName("Laptop Pro");
            mappedProduct.setDescription("A product description");
            mappedProduct.setQuantity(10);
            mappedProduct.setPrice(1200.0);
            mappedProduct.setDiscount(10.0);

            Product updatedProduct = buildProduct(productId, "Laptop Pro", 1200.0, 10.0, category);
            ProductRequest expectedResponse = buildProductRequest(productId, "Laptop Pro", 1200.0, 10.0, 1080.0);

            CartItem cartItem = new CartItem();
            cartItem.setProduct(existingProduct);
            Cart cart = new Cart(10L, null, List.of(cartItem), 900.0);
            CartDTO cartDTO = new CartDTO(10L, 900.0, new ArrayList<>());

            when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
            when(objectMapper.convertValue(updateRequest, Product.class)).thenReturn(mappedProduct);
            when(productRepository.save(existingProduct)).thenReturn(updatedProduct);
            when(cartRepository.findCartsByProductId(productId)).thenReturn(List.of(cart));
            when(objectMapper.convertValue(cart, CartDTO.class)).thenReturn(cartDTO);
            when(objectMapper.convertValue(updatedProduct, ProductRequest.class)).thenReturn(expectedResponse);

            // Act
            productService.updateProduct(updateRequest, productId);

            // Assert
            verify(cartService).updateProductInCarts(10L, productId);
        }

        @Test
        @DisplayName("Should recalculate special price on update")
        void shouldRecalculateSpecialPriceOnUpdate() {
            // Arrange
            Long productId = 1L;
            Category category = buildCategory(1L, "Electronics");
            Product existingProduct = buildProduct(productId, "Laptop", 1000.0, 10.0, category);

            ProductRequest updateRequest = buildProductRequest(productId, "Gaming Laptop", 2000.0, 25.0, null);
            Product mappedProduct = new Product();
            mappedProduct.setProductName("Gaming Laptop");
            mappedProduct.setDescription("A product description");
            mappedProduct.setQuantity(10);
            mappedProduct.setPrice(2000.0);
            mappedProduct.setDiscount(25.0);

            // specialPrice: 2000 - (25 * 0.01 * 2000) = 2000 - 500 = 1500
            Product updatedProduct = buildProduct(productId, "Gaming Laptop", 2000.0, 25.0, category);
            updatedProduct.setSpecialPrice(1500.0);
            ProductRequest expectedResponse = buildProductRequest(productId, "Gaming Laptop", 2000.0, 25.0, 1500.0);

            when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
            when(objectMapper.convertValue(updateRequest, Product.class)).thenReturn(mappedProduct);
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
                Product saved = invocation.getArgument(0);
                assertEquals(1500.0, saved.getSpecialPrice());
                return updatedProduct;
            });
            when(cartRepository.findCartsByProductId(productId)).thenReturn(Collections.emptyList());
            when(objectMapper.convertValue(updatedProduct, ProductRequest.class)).thenReturn(expectedResponse);

            // Act
            ProductRequest result = productService.updateProduct(updateRequest, productId);

            // Assert
            assertEquals(1500.0, result.getSpecialPrice());
        }
    }

    // ===========================================================================
    // updateProductImage
    // ===========================================================================

    @Nested
    @DisplayName("updateProductImage")
    class UpdateProductImageTests {

        @Test
        @DisplayName("Should successfully update the product image")
        void shouldSuccessfullyUpdateProductImage() throws IOException {
            // Arrange
            Long productId = 1L;
            Category category = buildCategory(1L, "Electronics");
            Product product = buildProduct(productId, "Laptop", 1000.0, 10.0, category);
            String newImageName = "laptop-new.png";
            Product updatedProduct = buildProduct(productId, "Laptop", 1000.0, 10.0, category);
            updatedProduct.setImage(newImageName);
            ProductRequest expectedResponse = buildProductRequest(productId, "Laptop", 1000.0, 10.0, 900.0);
            expectedResponse.setImage(newImageName);

            MultipartFile mockFile = mock(MultipartFile.class);

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(fileService.uploadImage("images/", mockFile)).thenReturn(newImageName);
            when(productRepository.save(product)).thenReturn(updatedProduct);
            when(objectMapper.convertValue(updatedProduct, ProductRequest.class)).thenReturn(expectedResponse);

            // Act
            ProductRequest result = productService.updateProductImage(productId, mockFile);

            // Assert
            assertNotNull(result);
            assertEquals(newImageName, result.getImage());
            verify(productRepository).findById(productId);
            verify(fileService).uploadImage("images/", mockFile);
            verify(productRepository).save(product);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when product does not exist")
        void shouldThrowResourceNotFoundExceptionWhenProductNotFound() throws IOException {
            // Arrange
            Long productId = 999L;
            MultipartFile mockFile = mock(MultipartFile.class);

            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                    productService.updateProductImage(productId, mockFile));
            assertEquals("Product not found with productId : 999", exception.getMessage());
            verify(productRepository).findById(productId);
            verify(fileService, never()).uploadImage(any(), any());
            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        @DisplayName("Should propagate IOException when file upload fails")
        void shouldPropagateIOExceptionWhenFileUploadFails() throws IOException {
            // Arrange
            Long productId = 1L;
            Category category = buildCategory(1L, "Electronics");
            Product product = buildProduct(productId, "Laptop", 1000.0, 10.0, category);
            MultipartFile mockFile = mock(MultipartFile.class);

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(fileService.uploadImage("images/", mockFile)).thenThrow(new IOException("Disk full"));

            // Act & Assert
            IOException exception = assertThrows(IOException.class, () ->
                    productService.updateProductImage(productId, mockFile));
            assertEquals("Disk full", exception.getMessage());
            verify(fileService).uploadImage("images/", mockFile);
            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        @DisplayName("Should set the returned filename from FileService onto the product before saving")
        void shouldSetReturnedFilenameOntoProduct() throws IOException {
            // Arrange
            Long productId = 1L;
            Category category = buildCategory(1L, "Electronics");
            Product product = buildProduct(productId, "Laptop", 1000.0, 10.0, category);
            String uploadedFileName = "abc123-laptop.jpg";
            Product savedProduct = buildProduct(productId, "Laptop", 1000.0, 10.0, category);
            savedProduct.setImage(uploadedFileName);
            ProductRequest expectedResponse = buildProductRequest(productId, "Laptop", 1000.0, 10.0, 900.0);

            MultipartFile mockFile = mock(MultipartFile.class);

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(fileService.uploadImage("images/", mockFile)).thenReturn(uploadedFileName);
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
                Product saved = invocation.getArgument(0);
                assertEquals(uploadedFileName, saved.getImage());
                return savedProduct;
            });
            when(objectMapper.convertValue(savedProduct, ProductRequest.class)).thenReturn(expectedResponse);

            // Act
            productService.updateProductImage(productId, mockFile);

            // Assert
            verify(productRepository).save(argThat(p -> uploadedFileName.equals(p.getImage())));
        }
    }
}

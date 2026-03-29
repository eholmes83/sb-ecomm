package com.echapps.ecom.project.product.repository;

import com.echapps.ecom.project.category.model.Category;
import com.echapps.ecom.project.category.repository.CategoryRepository;
import com.echapps.ecom.project.product.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ProductRepository layer.
 *
 * Testing Framework Choice: JUnit 5 (Jupiter) + Spring Boot @SpringBootTest
 * Rationale:
 * - @SpringBootTest matches the repository test style already established in this project
 * - @Transactional keeps each test isolated by rolling back DB changes after execution
 * - Embedded H2 allows fast, deterministic tests without any external database dependency
 * - @BeforeEach cleanup guards against state leaking between tests within the same transaction
 * - Nested test classes group tests by repository method, directly traceable to service call sites
 *
 * Test Coverage:
 * - Custom queries: findByCategoryOrderByPriceAsc, findByProductNameLikeIgnoreCase
 * - Inherited JPA operations: findAll(Pageable), findById, save, deleteById
 * - Edge cases: empty results, category isolation, pagination, case-insensitive matching
 */
@SpringBootTest
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("ProductRepository Unit Tests")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    // ---------------------------------------------------------------------------
    // Helper builders
    // ---------------------------------------------------------------------------

    private Category saveCategory(String name) {
        return categoryRepository.save(new Category(null, name, null));
    }

    private Product buildProduct(String name, Double price, Category category) {
        Product p = new Product();
        p.setProductName(name);
        p.setImage("default.png");
        p.setDescription("A valid description for testing");
        p.setQuantity(10);
        p.setPrice(price);
        p.setDiscount(0.0);
        p.setSpecialPrice(price);
        p.setCategory(category);
        return p;
    }

    // ===========================================================================
    // searchByCategory -> findByCategoryOrderByPriceAsc(Category, Pageable)
    // ===========================================================================

    @Nested
    @DisplayName("searchByCategory -> findByCategoryOrderByPriceAsc(Category, Pageable)")
    class FindByCategoryOrderByPriceAscTests {

        @Test
        @DisplayName("Should return products in the given category ordered by price ascending")
        void shouldReturnProductsInCategoryOrderedByPriceAsc() {
            Category category = saveCategory("Electronics");
            productRepository.save(buildProduct("Headphones", 150.0, category));
            productRepository.save(buildProduct("Laptop",     999.0, category));
            productRepository.save(buildProduct("Mouse",       30.0, category));

            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> page = productRepository.findByCategoryOrderByPriceAsc(category, pageable);

            assertEquals(3, page.getTotalElements());
            List<Product> content = page.getContent();
            assertEquals("Mouse",       content.get(0).getProductName());
            assertEquals("Headphones",  content.get(1).getProductName());
            assertEquals("Laptop",      content.get(2).getProductName());
            assertTrue(content.get(0).getPrice() <= content.get(1).getPrice());
            assertTrue(content.get(1).getPrice() <= content.get(2).getPrice());
        }

        @Test
        @DisplayName("Should only return products belonging to the specified category")
        void shouldOnlyReturnProductsInSpecifiedCategory() {
            Category electronics = saveCategory("Electronics");
            Category books       = saveCategory("Books");

            productRepository.save(buildProduct("Laptop",  999.0, electronics));
            productRepository.save(buildProduct("Phone",   699.0, electronics));
            productRepository.save(buildProduct("Java Guide", 50.0, books));

            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> page = productRepository.findByCategoryOrderByPriceAsc(electronics, pageable);

            assertEquals(2, page.getTotalElements());
            page.getContent().forEach(p ->
                    assertEquals("Electronics", p.getCategory().getCategoryName()));
        }

        @Test
        @DisplayName("Should return empty page when the category has no products")
        void shouldReturnEmptyPageWhenCategoryHasNoProducts() {
            Category emptyCategory = saveCategory("Toys");

            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> page = productRepository.findByCategoryOrderByPriceAsc(emptyCategory, pageable);

            assertTrue(page.isEmpty());
            assertEquals(0, page.getTotalElements());
        }

        @Test
        @DisplayName("Should paginate results correctly across multiple pages")
        void shouldPaginateResultsCorrectly() {
            Category category = saveCategory("Electronics");
            productRepository.save(buildProduct("Item A", 10.0, category));
            productRepository.save(buildProduct("Item B", 20.0, category));
            productRepository.save(buildProduct("Item C", 30.0, category));
            productRepository.save(buildProduct("Item D", 40.0, category));
            productRepository.save(buildProduct("Item E", 50.0, category));

            Pageable firstPage  = PageRequest.of(0, 2);
            Pageable secondPage = PageRequest.of(1, 2);

            Page<Product> page0 = productRepository.findByCategoryOrderByPriceAsc(category, firstPage);
            Page<Product> page1 = productRepository.findByCategoryOrderByPriceAsc(category, secondPage);

            assertEquals(5, page0.getTotalElements());
            assertEquals(2, page0.getContent().size());
            assertFalse(page0.isLast());

            assertEquals(2, page1.getContent().size());
            // page 0 has cheapest two items, page 1 has the next two
            assertTrue(page0.getContent().get(0).getPrice() < page1.getContent().get(0).getPrice());
        }

        @Test
        @DisplayName("Should return a single product when only one exists in the category")
        void shouldReturnSingleProductWhenOnlyOneExistsInCategory() {
            Category category = saveCategory("Books");
            Product saved = productRepository.save(buildProduct("Clean Code", 45.0, category));

            Page<Product> page = productRepository.findByCategoryOrderByPriceAsc(
                    category, PageRequest.of(0, 10));

            assertEquals(1, page.getTotalElements());
            assertEquals(saved.getProductId(), page.getContent().get(0).getProductId());
        }
    }

    // ===========================================================================
    // searchProductsByKeyword -> findByProductNameLikeIgnoreCase(String, Pageable)
    // ===========================================================================

    @Nested
    @DisplayName("searchProductsByKeyword -> findByProductNameLikeIgnoreCase(String, Pageable)")
    class FindByProductNameLikeIgnoreCaseTests {

        @Test
        @DisplayName("Should return products whose name contains the keyword (exact case)")
        void shouldMatchKeywordExactCase() {
            productRepository.save(buildProduct("Laptop Pro", 1200.0, null));
            productRepository.save(buildProduct("Keyboard",    80.0, null));

            Page<Product> page = productRepository.findByProductNameLikeIgnoreCase(
                    "%Laptop%", PageRequest.of(0, 10));

            assertEquals(1, page.getTotalElements());
            assertEquals("Laptop Pro", page.getContent().get(0).getProductName());
        }

        @Test
        @DisplayName("Should match keyword case-insensitively when keyword is uppercase")
        void shouldMatchKeywordCaseInsensitiveUppercase() {
            productRepository.save(buildProduct("Smartphone", 699.0, null));
            productRepository.save(buildProduct("Tablet",     499.0, null));

            Page<Product> page = productRepository.findByProductNameLikeIgnoreCase(
                    "%SMARTPHONE%", PageRequest.of(0, 10));

            assertEquals(1, page.getTotalElements());
            assertEquals("Smartphone", page.getContent().get(0).getProductName());
        }

        @Test
        @DisplayName("Should match keyword case-insensitively when keyword is lowercase")
        void shouldMatchKeywordCaseInsensitiveLowercase() {
            productRepository.save(buildProduct("Gaming Mouse", 55.0, null));
            productRepository.save(buildProduct("USB Hub",      25.0, null));

            Page<Product> page = productRepository.findByProductNameLikeIgnoreCase(
                    "%gaming%", PageRequest.of(0, 10));

            assertEquals(1, page.getTotalElements());
            assertEquals("Gaming Mouse", page.getContent().get(0).getProductName());
        }

        @Test
        @DisplayName("Should return multiple products when several names contain the keyword")
        void shouldReturnMultipleProductsMatchingKeyword() {
            productRepository.save(buildProduct("Laptop Air",   999.0, null));
            productRepository.save(buildProduct("Laptop Pro",  1299.0, null));
            productRepository.save(buildProduct("Desktop",      799.0, null));

            Page<Product> page = productRepository.findByProductNameLikeIgnoreCase(
                    "%Laptop%", PageRequest.of(0, 10));

            assertEquals(2, page.getTotalElements());
        }

        @Test
        @DisplayName("Should return empty page when no product names match the keyword")
        void shouldReturnEmptyPageWhenNoMatchFound() {
            productRepository.save(buildProduct("Laptop", 999.0, null));
            productRepository.save(buildProduct("Phone",  699.0, null));

            Page<Product> page = productRepository.findByProductNameLikeIgnoreCase(
                    "%xyznotfound%", PageRequest.of(0, 10));

            assertTrue(page.isEmpty());
            assertEquals(0, page.getTotalElements());
        }

        @Test
        @DisplayName("Should paginate keyword search results correctly")
        void shouldPaginateKeywordSearchResultsCorrectly() {
            productRepository.save(buildProduct("Pro Item 1", 10.0, null));
            productRepository.save(buildProduct("Pro Item 2", 20.0, null));
            productRepository.save(buildProduct("Pro Item 3", 30.0, null));
            productRepository.save(buildProduct("Basic Item", 40.0, null));

            Page<Product> firstPage = productRepository.findByProductNameLikeIgnoreCase(
                    "%Pro%", PageRequest.of(0, 2));

            assertEquals(3, firstPage.getTotalElements());
            assertEquals(2, firstPage.getContent().size());
            assertFalse(firstPage.isLast());
        }

        @Test
        @DisplayName("Should return empty page when the repository has no products at all")
        void shouldReturnEmptyPageWhenRepositoryIsEmpty() {
            Page<Product> page = productRepository.findByProductNameLikeIgnoreCase(
                    "%phone%", PageRequest.of(0, 10));

            assertTrue(page.isEmpty());
        }
    }

    // ===========================================================================
    // getAllProducts -> findAll(Pageable)
    // ===========================================================================

    @Nested
    @DisplayName("getAllProducts -> findAll(Pageable)")
    class FindAllPageableTests {

        @Test
        @DisplayName("Should return all products in a single page when count is within page size")
        void shouldReturnAllProductsInSinglePage() {
            productRepository.save(buildProduct("Laptop",  999.0, null));
            productRepository.save(buildProduct("Phone",   699.0, null));
            productRepository.save(buildProduct("Tablet",  499.0, null));

            Page<Product> page = productRepository.findAll(PageRequest.of(0, 10));

            assertEquals(3, page.getTotalElements());
            assertEquals(3, page.getContent().size());
        }

        @Test
        @DisplayName("Should return empty page when no products exist")
        void shouldReturnEmptyPageWhenNoProductsExist() {
            Page<Product> page = productRepository.findAll(PageRequest.of(0, 10));

            assertTrue(page.isEmpty());
            assertEquals(0, page.getTotalElements());
        }

        @Test
        @DisplayName("Should sort products by price ascending")
        void shouldSortProductsByPriceAscending() {
            productRepository.save(buildProduct("Laptop",  999.0, null));
            productRepository.save(buildProduct("Mouse",    25.0, null));
            productRepository.save(buildProduct("Monitor", 350.0, null));

            Pageable pageable = PageRequest.of(0, 10, Sort.by("price").ascending());
            Page<Product> page = productRepository.findAll(pageable);

            List<Product> content = page.getContent();
            assertEquals("Mouse",   content.get(0).getProductName());
            assertEquals("Monitor", content.get(1).getProductName());
            assertEquals("Laptop",  content.get(2).getProductName());
        }

        @Test
        @DisplayName("Should sort products by productName descending")
        void shouldSortProductsByNameDescending() {
            productRepository.save(buildProduct("Apple Watch", 399.0, null));
            productRepository.save(buildProduct("Bluetooth Speaker", 89.0, null));
            productRepository.save(buildProduct("Camera",     499.0, null));

            Pageable pageable = PageRequest.of(0, 10, Sort.by("productName").descending());
            Page<Product> page = productRepository.findAll(pageable);

            List<Product> content = page.getContent();
            assertEquals("Camera",           content.get(0).getProductName());
            assertEquals("Bluetooth Speaker", content.get(1).getProductName());
            assertEquals("Apple Watch",      content.get(2).getProductName());
        }

        @Test
        @DisplayName("Should correctly report pagination metadata across multiple pages")
        void shouldReportCorrectPaginationMetadata() {
            for (int i = 1; i <= 5; i++) {
                productRepository.save(buildProduct("Product " + i, (double) i * 10, null));
            }

            Page<Product> page0 = productRepository.findAll(PageRequest.of(0, 2));
            Page<Product> page2 = productRepository.findAll(PageRequest.of(2, 2));

            assertEquals(5, page0.getTotalElements());
            assertEquals(3, page0.getTotalPages());
            assertEquals(2, page0.getContent().size());
            assertFalse(page0.isLast());

            assertEquals(1, page2.getContent().size());
            assertTrue(page2.isLast());
        }
    }

    // ===========================================================================
    // deleteProduct / updateProduct -> findById(Long)
    // ===========================================================================

    @Nested
    @DisplayName("deleteProduct/updateProduct -> findById(Long)")
    class FindByIdTests {

        @Test
        @DisplayName("Should return product when the given ID exists")
        void shouldReturnProductWhenIdExists() {
            Product saved = productRepository.save(buildProduct("Laptop", 999.0, null));

            Optional<Product> found = productRepository.findById(saved.getProductId());

            assertTrue(found.isPresent());
            assertEquals("Laptop", found.get().getProductName());
            assertEquals(999.0, found.get().getPrice());
        }

        @Test
        @DisplayName("Should return empty Optional when the given ID does not exist")
        void shouldReturnEmptyOptionalWhenIdDoesNotExist() {
            Optional<Product> found = productRepository.findById(99999L);

            assertTrue(found.isEmpty());
        }

        @Test
        @DisplayName("Should return correct product when multiple products exist")
        void shouldReturnCorrectProductWhenMultipleExist() {
            Product laptop = productRepository.save(buildProduct("Laptop", 999.0, null));
            Product phone  = productRepository.save(buildProduct("Phone",  699.0, null));

            Optional<Product> found = productRepository.findById(phone.getProductId());

            assertTrue(found.isPresent());
            assertEquals("Phone", found.get().getProductName());
            assertNotEquals(laptop.getProductId(), found.get().getProductId());
        }
    }

    // ===========================================================================
    // addProduct / updateProduct -> save(Product)
    // ===========================================================================

    @Nested
    @DisplayName("addProduct/updateProduct -> save(Product)")
    class SaveTests {

        @Test
        @DisplayName("Should persist a new product and generate a non-null ID")
        void shouldPersistNewProductAndGenerateId() {
            Product product = buildProduct("Laptop", 999.0, null);

            Product saved = productRepository.save(product);

            assertNotNull(saved.getProductId());
            assertEquals("Laptop", saved.getProductName());
            assertEquals(1, productRepository.count());
        }

        @Test
        @DisplayName("Should correctly store all fields when saving a new product")
        void shouldStoreAllFieldsCorrectly() {
            Category category = saveCategory("Electronics");
            Product product = buildProduct("Mechanical Keyboard", 120.0, category);
            product.setDiscount(10.0);
            product.setSpecialPrice(108.0);
            product.setQuantity(50);

            Product saved = productRepository.save(product);
            Product fetched = productRepository.findById(saved.getProductId()).orElseThrow();

            assertEquals("Mechanical Keyboard", fetched.getProductName());
            assertEquals("default.png",         fetched.getImage());
            assertEquals(120.0,                 fetched.getPrice());
            assertEquals(10.0,                  fetched.getDiscount());
            assertEquals(108.0,                 fetched.getSpecialPrice());
            assertEquals(50,                    fetched.getQuantity());
            assertEquals("Electronics",         fetched.getCategory().getCategoryName());
        }

        @Test
        @DisplayName("Should update an existing product when saving with the same ID")
        void shouldUpdateExistingProductWhenSavingWithSameId() {
            Product saved = productRepository.save(buildProduct("Old Name", 100.0, null));
            Long id = saved.getProductId();

            saved.setProductName("New Name");
            saved.setPrice(200.0);
            Product updated = productRepository.save(saved);

            assertEquals(id,         updated.getProductId());
            assertEquals("New Name", updated.getProductName());
            assertEquals(200.0,      updated.getPrice());
            assertEquals(1,          productRepository.count());
        }

        @Test
        @DisplayName("Should persist product without a category (category is nullable)")
        void shouldPersistProductWithoutCategory() {
            Product product = buildProduct("Standalone Item", 50.0, null);

            Product saved = productRepository.save(product);

            assertNotNull(saved.getProductId());
            assertNull(productRepository.findById(saved.getProductId())
                    .orElseThrow().getCategory());
        }
    }

    // ===========================================================================
    // deleteProduct -> deleteById(Long)
    // ===========================================================================

    @Nested
    @DisplayName("deleteProduct -> deleteById(Long)")
    class DeleteByIdTests {

        @Test
        @DisplayName("Should remove the product so it no longer exists in the repository")
        void shouldDeleteProductById() {
            Product saved = productRepository.save(buildProduct("Laptop", 999.0, null));
            Long id = saved.getProductId();

            productRepository.deleteById(id);

            assertTrue(productRepository.findById(id).isEmpty());
            assertEquals(0, productRepository.count());
        }

        @Test
        @DisplayName("Should not affect other products when deleting a specific product")
        void shouldNotAffectOtherProductsWhenDeletingOne() {
            Product laptop = productRepository.save(buildProduct("Laptop", 999.0, null));
            Product phone  = productRepository.save(buildProduct("Phone",  699.0, null));

            productRepository.deleteById(laptop.getProductId());

            assertEquals(1, productRepository.count());
            assertTrue(productRepository.findById(phone.getProductId()).isPresent());
            assertTrue(productRepository.findById(laptop.getProductId()).isEmpty());
        }

        @Test
        @DisplayName("Should leave the repository empty after deleting the only product")
        void shouldLeaveRepositoryEmptyAfterDeletingOnlyProduct() {
            Product saved = productRepository.save(buildProduct("Sole Product", 10.0, null));

            productRepository.deleteById(saved.getProductId());

            assertEquals(0, productRepository.count());
        }
    }
}

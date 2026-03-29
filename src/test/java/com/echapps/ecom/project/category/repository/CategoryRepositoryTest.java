package com.echapps.ecom.project.category.repository;

import com.echapps.ecom.project.category.model.Category;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CategoryRepository layer.
 *
 * Testing Framework Choice: JUnit 5 (Jupiter) + Spring Boot @SpringBootTest
 * Rationale:
 * - JUnit 5 matches the existing service/controller test style and supports nested test grouping
 * - @SpringBootTest guarantees compatibility with the Spring Boot 4 test setup already used in this project
 * - @Transactional keeps each test isolated by rolling back DB changes after execution
 * - Embedded H2 allows fast and deterministic tests without external DB dependency
 * - Tests are mapped to repository calls used by CategoryServiceImpl for direct traceability
 */
@SpringBootTest
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("CategoryRepository Unit Tests")
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
    }

    @Nested
    @DisplayName("getAllCategories -> findAll(Pageable)")
    class FindAllPageableTests {

        @Test
        @DisplayName("Should return ascending page by categoryId")
        void shouldReturnAscendingPageByCategoryId() {
            categoryRepository.save(new Category(null, "Books", null));
            categoryRepository.save(new Category(null, "Electronics", null));

            Pageable pageable = PageRequest.of(0, 10, Sort.by("categoryId").ascending());
            Page<Category> page = categoryRepository.findAll(pageable);

            assertEquals(2, page.getTotalElements());
            assertEquals(2, page.getContent().size());
            assertTrue(page.getContent().get(0).getCategoryId() < page.getContent().get(1).getCategoryId());
        }

        @Test
        @DisplayName("Should return descending page by categoryName")
        void shouldReturnDescendingPageByCategoryName() {
            categoryRepository.save(new Category(null, "Books", null));
            categoryRepository.save(new Category(null, "Electronics", null));

            Pageable pageable = PageRequest.of(0, 10, Sort.by("categoryName").descending());
            Page<Category> page = categoryRepository.findAll(pageable);

            assertEquals(2, page.getTotalElements());
            assertEquals("Electronics", page.getContent().get(0).getCategoryName());
            assertEquals("Books", page.getContent().get(1).getCategoryName());
        }

        @Test
        @DisplayName("Should return empty page when repository has no categories")
        void shouldReturnEmptyPageWhenNoCategories() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by("categoryId").ascending());

            Page<Category> page = categoryRepository.findAll(pageable);

            assertTrue(page.isEmpty());
            assertEquals(0, page.getTotalElements());
        }
    }

    @Nested
    @DisplayName("createCategory -> findByCategoryName(String)")
    class FindByCategoryNameTests {

        @Test
        @DisplayName("Should return category for exact name match")
        void shouldReturnCategoryForExactNameMatch() {
            categoryRepository.save(new Category(null, "Electronics", null));

            Category found = categoryRepository.findByCategoryName("Electronics");

            assertNotNull(found);
            assertEquals("Electronics", found.getCategoryName());
        }

        @Test
        @DisplayName("Should return null when category name does not exist")
        void shouldReturnNullWhenCategoryNameDoesNotExist() {
            categoryRepository.save(new Category(null, "Electronics", null));

            Category found = categoryRepository.findByCategoryName("Fashion");

            assertNull(found);
        }

        @Test
        @DisplayName("Should be case sensitive for category name lookup")
        void shouldBeCaseSensitiveForCategoryNameLookup() {
            categoryRepository.save(new Category(null, "Electronics", null));

            Category found = categoryRepository.findByCategoryName("electronics");

            assertNull(found);
        }
    }

    @Nested
    @DisplayName("deleteCategory/updateCategory -> findById(Long)")
    class FindByIdTests {

        @Test
        @DisplayName("Should return category when id exists")
        void shouldReturnCategoryWhenIdExists() {
            Category saved = categoryRepository.save(new Category(null, "Books", null));

            Optional<Category> found = categoryRepository.findById(saved.getCategoryId());

            assertTrue(found.isPresent());
            assertEquals("Books", found.get().getCategoryName());
        }

        @Test
        @DisplayName("Should return empty optional when id does not exist")
        void shouldReturnEmptyWhenIdDoesNotExist() {
            Optional<Category> found = categoryRepository.findById(99999L);

            assertTrue(found.isEmpty());
        }
    }

    @Nested
    @DisplayName("createCategory/updateCategory -> save(Category)")
    class SaveTests {

        @Test
        @DisplayName("Should persist new category and generate id")
        void shouldPersistNewCategoryAndGenerateId() {
            Category saved = categoryRepository.save(new Category(null, "Sports", null));

            assertNotNull(saved.getCategoryId());
            assertEquals("Sports", saved.getCategoryName());
            assertEquals(1, categoryRepository.count());
        }

        @Test
        @DisplayName("Should update existing category when saving with existing id")
        void shouldUpdateExistingCategoryWhenSavingWithExistingId() {
            Category saved = categoryRepository.save(new Category(null, "Home", null));
            Long id = saved.getCategoryId();

            saved.setCategoryName("Home Updated");
            Category updated = categoryRepository.save(saved);

            assertEquals(id, updated.getCategoryId());
            assertEquals("Home Updated", updated.getCategoryName());
            assertEquals(1, categoryRepository.count());
        }
    }

    @Nested
    @DisplayName("deleteCategory -> delete(Category)")
    class DeleteTests {

        @Test
        @DisplayName("Should delete existing category entity")
        void shouldDeleteExistingCategoryEntity() {
            Category saved = categoryRepository.save(new Category(null, "Accessories", null));

            categoryRepository.delete(saved);

            assertTrue(categoryRepository.findById(saved.getCategoryId()).isEmpty());
            assertEquals(0, categoryRepository.count());
        }

        @Test
        @DisplayName("Should not affect other categories when deleting one")
        void shouldNotAffectOtherCategoriesWhenDeletingOne() {
            Category first = categoryRepository.save(new Category(null, "Books", null));
            Category second = categoryRepository.save(new Category(null, "Electronics", null));

            categoryRepository.delete(first);

            assertEquals(1, categoryRepository.count());
            Optional<Category> remaining = categoryRepository.findById(second.getCategoryId());
            assertTrue(remaining.isPresent());
            assertEquals("Electronics", remaining.get().getCategoryName());
        }
    }
}

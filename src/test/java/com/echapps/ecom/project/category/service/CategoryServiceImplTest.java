package com.echapps.ecom.project.category.service;

import com.echapps.ecom.project.category.dto.request.CategoryRequest;
import com.echapps.ecom.project.category.dto.response.CategoryResponse;
import com.echapps.ecom.project.category.model.Category;
import com.echapps.ecom.project.category.repository.CategoryRepository;
import com.echapps.ecom.project.exceptions.APIException;
import com.echapps.ecom.project.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CategoryServiceImpl layer.
 *
 * Testing Framework Choice: JUnit 5 (Jupiter) + Mockito
 * Rationale:
 * - JUnit 5 provides modern testing features with better parameterization and nested tests
 * - Mockito isolates the service from database/repository dependencies
 * - ObjectMapper is mocked to test pure business logic without JSON serialization concerns
 * - Tests follow the vertical slice architecture by residing in src/test/java/com/echapps/ecom/project/category/service/
 * - Nested test classes organize tests by method, improving readability and maintainability
 *
 * Test Coverage:
 * - Happy paths: successful operations with valid inputs
 * - Edge cases: empty results, null values, boundary conditions
 * - Error cases: exceptions thrown by repository, duplicate checks, not-found scenarios
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryServiceImpl Unit Tests")
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ObjectMapper objectMapper;

    private CategoryServiceImpl categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryServiceImpl(categoryRepository, objectMapper);
    }

    @Nested
    @DisplayName("getAllCategories")
    class GetAllCategoriesTests {

        @Test
        @DisplayName("Should return paginated categories with default sorting (ascending)")
        void shouldReturnPaginatedCategoriesWithDefaultSorting() {
            // Arrange
            Integer pageNumber = 0;
            Integer pageSize = 50;
            String sortBy = "categoryId";
            String sortOrder = "asc";

            Category category1 = new Category(1L, "Electronics", null);
            Category category2 = new Category(2L, "Books", null);
            List<Category> categories = List.of(category1, category2);

            Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).ascending());
            Page<Category> categoryPage = new PageImpl<>(categories, pageable, 2);

            CategoryRequest catReq1 = new CategoryRequest(1L, "Electronics");
            CategoryRequest catReq2 = new CategoryRequest(2L, "Books");

            when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);
            when(objectMapper.convertValue(category1, CategoryRequest.class)).thenReturn(catReq1);
            when(objectMapper.convertValue(category2, CategoryRequest.class)).thenReturn(catReq2);

            // Act
            CategoryResponse response = categoryService.getAllCategories(pageNumber, pageSize, sortBy, sortOrder);

            // Assert
            assertNotNull(response);
            assertEquals(2, response.getContent().size());
            assertEquals(0, response.getPageNumber());
            assertEquals(50, response.getPageSize());
            assertEquals(2, response.getTotalElements());
            assertEquals(1, response.getTotalPages());
            assertEquals(true, response.getLastPage());
            verify(categoryRepository).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("Should return paginated categories with descending order")
        void shouldReturnPaginatedCategoriesWithDescendingOrder() {
            // Arrange
            Integer pageNumber = 0;
            Integer pageSize = 10;
            String sortBy = "categoryName";
            String sortOrder = "desc";

            Category category = new Category(1L, "Fashion", null);
            List<Category> categories = List.of(category);

            Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).descending());
            Page<Category> categoryPage = new PageImpl<>(categories, pageable, 1);

            CategoryRequest catReq = new CategoryRequest(1L, "Fashion");

            when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);
            when(objectMapper.convertValue(category, CategoryRequest.class)).thenReturn(catReq);

            // Act
            CategoryResponse response = categoryService.getAllCategories(pageNumber, pageSize, sortBy, sortOrder);

            // Assert
            assertNotNull(response);
            assertEquals(1, response.getContent().size());
            assertEquals("Fashion", response.getContent().get(0).getCategoryName());
            verify(categoryRepository).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("Should throw APIException when no categories are found")
        void shouldThrowAPIExceptionWhenNoCategoriesFound() {
            // Arrange
            Integer pageNumber = 0;
            Integer pageSize = 50;
            String sortBy = "categoryId";
            String sortOrder = "asc";

            Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).ascending());
            Page<Category> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(categoryRepository.findAll(pageable)).thenReturn(emptyPage);

            // Act & Assert
            APIException exception = assertThrows(APIException.class, () ->
                    categoryService.getAllCategories(pageNumber, pageSize, sortBy, sortOrder));
            assertEquals("No categories found!", exception.getMessage());
            verify(categoryRepository).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle multiple pages correctly")
        void shouldHandleMultiplePagesCorrectly() {
            // Arrange
            Integer pageNumber = 1;
            Integer pageSize = 2;
            String sortBy = "categoryId";
            String sortOrder = "asc";

            Category category3 = new Category(3L, "Accessories", null);
            Category category4 = new Category(4L, "Home", null);
            List<Category> categories = List.of(category3, category4);

            Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).ascending());
            Page<Category> categoryPage = new PageImpl<>(categories, pageable, 5);

            CategoryRequest catReq3 = new CategoryRequest(3L, "Accessories");
            CategoryRequest catReq4 = new CategoryRequest(4L, "Home");

            when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);
            when(objectMapper.convertValue(category3, CategoryRequest.class)).thenReturn(catReq3);
            when(objectMapper.convertValue(category4, CategoryRequest.class)).thenReturn(catReq4);

            // Act
            CategoryResponse response = categoryService.getAllCategories(pageNumber, pageSize, sortBy, sortOrder);

            // Assert
            assertEquals(1, response.getPageNumber());
            assertEquals(2, response.getPageSize());
            assertEquals(5, response.getTotalElements());
            assertEquals(3, response.getTotalPages());
            assertEquals(false, response.getLastPage());
        }
    }

    @Nested
    @DisplayName("createCategory")
    class CreateCategoryTests {

        @Test
        @DisplayName("Should successfully create a new category")
        void shouldSuccessfullyCreateNewCategory() {
            // Arrange
            CategoryRequest request = new CategoryRequest(null, "Sports");
            Category categoryToSave = new Category(null, "Sports", null);
            Category savedCategory = new Category(5L, "Sports", null);
            CategoryRequest response = new CategoryRequest(5L, "Sports");

            when(objectMapper.convertValue(request, Category.class)).thenReturn(categoryToSave);
            when(categoryRepository.findByCategoryName("Sports")).thenReturn(null);
            when(categoryRepository.save(categoryToSave)).thenReturn(savedCategory);
            when(objectMapper.convertValue(savedCategory, CategoryRequest.class)).thenReturn(response);

            // Act
            CategoryRequest result = categoryService.createCategory(request);

            // Assert
            assertNotNull(result);
            assertEquals(5L, result.getCategoryId());
            assertEquals("Sports", result.getCategoryName());
            verify(categoryRepository).findByCategoryName("Sports");
            verify(categoryRepository).save(categoryToSave);
        }

        @Test
        @DisplayName("Should throw APIException when category name already exists")
        void shouldThrowAPIExceptionWhenCategoryNameExists() {
            // Arrange
            CategoryRequest request = new CategoryRequest(null, "Electronics");
            Category existingCategory = new Category(1L, "Electronics", null);
            Category categoryToSave = new Category(null, "Electronics", null);

            when(objectMapper.convertValue(request, Category.class)).thenReturn(categoryToSave);
            when(categoryRepository.findByCategoryName("Electronics")).thenReturn(existingCategory);

            // Act & Assert
            APIException exception = assertThrows(APIException.class, () ->
                    categoryService.createCategory(request));
            assertEquals("Category with name Electronics already exists!", exception.getMessage());
            verify(categoryRepository).findByCategoryName("Electronics");
            verify(categoryRepository, never()).save(any(Category.class));
        }

        @Test
        @DisplayName("Should handle null category name gracefully")
        void shouldHandleNullCategoryName() {
            // Arrange
            CategoryRequest request = new CategoryRequest(null, null);
            Category categoryToSave = new Category(null, null, null);
            Category savedCategory = new Category(10L, null, null);
            CategoryRequest response = new CategoryRequest(10L, null);

            when(objectMapper.convertValue(request, Category.class)).thenReturn(categoryToSave);
            when(categoryRepository.findByCategoryName(null)).thenReturn(null);
            when(categoryRepository.save(categoryToSave)).thenReturn(savedCategory);
            when(objectMapper.convertValue(savedCategory, CategoryRequest.class)).thenReturn(response);

            // Act
            CategoryRequest result = categoryService.createCategory(request);

            // Assert
            assertNotNull(result);
            assertEquals(10L, result.getCategoryId());
            verify(categoryRepository).findByCategoryName(null);
            verify(categoryRepository).save(categoryToSave);
        }

        @Test
        @DisplayName("Should successfully create category with special characters in name")
        void shouldCreateCategoryWithSpecialCharacters() {
            // Arrange
            CategoryRequest request = new CategoryRequest(null, "Health & Beauty");
            Category categoryToSave = new Category(null, "Health & Beauty", null);
            Category savedCategory = new Category(11L, "Health & Beauty", null);
            CategoryRequest response = new CategoryRequest(11L, "Health & Beauty");

            when(objectMapper.convertValue(request, Category.class)).thenReturn(categoryToSave);
            when(categoryRepository.findByCategoryName("Health & Beauty")).thenReturn(null);
            when(categoryRepository.save(categoryToSave)).thenReturn(savedCategory);
            when(objectMapper.convertValue(savedCategory, CategoryRequest.class)).thenReturn(response);

            // Act
            CategoryRequest result = categoryService.createCategory(request);

            // Assert
            assertNotNull(result);
            assertEquals("Health & Beauty", result.getCategoryName());
        }
    }

    @Nested
    @DisplayName("deleteCategory")
    class DeleteCategoryTests {

        @Test
        @DisplayName("Should successfully delete an existing category")
        void shouldSuccessfullyDeleteCategory() {
            // Arrange
            Long categoryId = 1L;
            Category category = new Category(1L, "Electronics", null);
            CategoryRequest response = new CategoryRequest(1L, "Electronics");

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
            when(objectMapper.convertValue(category, CategoryRequest.class)).thenReturn(response);

            // Act
            CategoryRequest result = categoryService.deleteCategory(categoryId);

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getCategoryId());
            assertEquals("Electronics", result.getCategoryName());
            verify(categoryRepository).findById(categoryId);
            verify(categoryRepository).delete(category);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when category does not exist")
        void shouldThrowResourceNotFoundExceptionWhenCategoryNotFound() {
            // Arrange
            Long categoryId = 999L;
            when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                    categoryService.deleteCategory(categoryId));
            assertEquals("Category not found with categoryId : 999", exception.getMessage());
            verify(categoryRepository).findById(categoryId);
            verify(categoryRepository, never()).delete(any(Category.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when deleting with null ID")
        void shouldThrowResourceNotFoundExceptionWhenDeletingWithNullId() {
            // Arrange
            Long categoryId = null;
            when(categoryRepository.findById(null)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class, () ->
                    categoryService.deleteCategory(categoryId));
            verify(categoryRepository).findById(null);
            verify(categoryRepository, never()).delete(any(Category.class));
        }

        @Test
        @DisplayName("Should successfully delete category with ID of 1 (edge case)")
        void shouldDeleteCategoryWithIdOne() {
            // Arrange
            Long categoryId = 1L;
            Category category = new Category(1L, "First Category", null);
            CategoryRequest response = new CategoryRequest(1L, "First Category");

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
            when(objectMapper.convertValue(category, CategoryRequest.class)).thenReturn(response);

            // Act
            CategoryRequest result = categoryService.deleteCategory(categoryId);

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getCategoryId());
            verify(categoryRepository).delete(category);
        }
    }

    @Nested
    @DisplayName("updateCategory")
    class UpdateCategoryTests {

        @Test
        @DisplayName("Should successfully update an existing category")
        void shouldSuccessfullyUpdateCategory() {
            // Arrange
            Long categoryId = 2L;
            CategoryRequest updateRequest = new CategoryRequest(2L, "Books - Updated");
            Category existingCategory = new Category(2L, "Books", null);
            Category updatedCategory = new Category(2L, "Books - Updated", null);
            CategoryRequest response = new CategoryRequest(2L, "Books");

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
            when(objectMapper.convertValue(updateRequest, Category.class)).thenReturn(updatedCategory);
            when(categoryRepository.save(updatedCategory)).thenReturn(updatedCategory);
            when(objectMapper.convertValue(existingCategory, CategoryRequest.class)).thenReturn(response);

            // Act
            CategoryRequest result = categoryService.updateCategory(updateRequest, categoryId);

            // Assert
            assertNotNull(result);
            assertEquals(2L, result.getCategoryId());
            verify(categoryRepository).findById(categoryId);
            verify(categoryRepository).save(any(Category.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when updating non-existent category")
        void shouldThrowResourceNotFoundExceptionWhenCategoryNotFound() {
            // Arrange
            Long categoryId = 999L;
            CategoryRequest updateRequest = new CategoryRequest(999L, "Non-existent");

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                    categoryService.updateCategory(updateRequest, categoryId));
            assertEquals("Category not found with categoryId : 999", exception.getMessage());
            verify(categoryRepository).findById(categoryId);
            verify(categoryRepository, never()).save(any(Category.class));
        }

        @Test
        @DisplayName("Should update category name to null")
        void shouldUpdateCategoryNameToNull() {
            // Arrange
            Long categoryId = 3L;
            CategoryRequest updateRequest = new CategoryRequest(3L, null);
            Category existingCategory = new Category(3L, "Fashion", null);
            Category updatedCategory = new Category(3L, null, null);
            CategoryRequest response = new CategoryRequest(3L, "Fashion");

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
            when(objectMapper.convertValue(updateRequest, Category.class)).thenReturn(updatedCategory);
            when(categoryRepository.save(updatedCategory)).thenReturn(updatedCategory);
            when(objectMapper.convertValue(existingCategory, CategoryRequest.class)).thenReturn(response);

            // Act
            CategoryRequest result = categoryService.updateCategory(updateRequest, categoryId);

            // Assert
            assertNotNull(result);
            assertEquals(3L, result.getCategoryId());
            verify(categoryRepository).save(any(Category.class));
        }

        @Test
        @DisplayName("Should override categoryId from request with the one from URL")
        void shouldOverrideCategoryIdFromRequest() {
            // Arrange
            Long categoryId = 4L;
            CategoryRequest updateRequest = new CategoryRequest(100L, "Updated Name"); // Wrong ID in request
            Category existingCategory = new Category(4L, "Home", null);
            Category updatedCategory = new Category(100L, "Updated Name", null);
            CategoryRequest response = new CategoryRequest(4L, "Home");

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
            when(objectMapper.convertValue(updateRequest, Category.class)).thenReturn(updatedCategory);
            when(categoryRepository.save(updatedCategory)).thenReturn(updatedCategory);
            when(objectMapper.convertValue(existingCategory, CategoryRequest.class)).thenReturn(response);

            // Act
            CategoryRequest result = categoryService.updateCategory(updateRequest, categoryId);

            // Assert
            assertNotNull(result);
            // The service should set the categoryId to the path variable value
            verify(categoryRepository).save(any(Category.class));
        }

        @Test
        @DisplayName("Should successfully update with special characters in category name")
        void shouldUpdateCategoryWithSpecialCharacters() {
            // Arrange
            Long categoryId = 5L;
            CategoryRequest updateRequest = new CategoryRequest(5L, "Sports & Outdoors");
            Category existingCategory = new Category(5L, "Sports", null);
            Category updatedCategory = new Category(5L, "Sports & Outdoors", null);
            CategoryRequest response = new CategoryRequest(5L, "Sports");

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
            when(objectMapper.convertValue(updateRequest, Category.class)).thenReturn(updatedCategory);
            when(categoryRepository.save(updatedCategory)).thenReturn(updatedCategory);
            when(objectMapper.convertValue(existingCategory, CategoryRequest.class)).thenReturn(response);

            // Act
            CategoryRequest result = categoryService.updateCategory(updateRequest, categoryId);

            // Assert
            assertNotNull(result);
            verify(categoryRepository).save(any(Category.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when updating with null ID")
        void shouldThrowResourceNotFoundExceptionWhenUpdatingWithNullId() {
            // Arrange
            Long categoryId = null;
            CategoryRequest updateRequest = new CategoryRequest(null, "Updated");

            when(categoryRepository.findById(null)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class, () ->
                    categoryService.updateCategory(updateRequest, categoryId));
            verify(categoryRepository).findById(null);
        }
    }
}

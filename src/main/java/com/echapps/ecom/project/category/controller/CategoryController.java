package com.echapps.ecom.project.category.controller;

import com.echapps.ecom.project.category.dto.request.CategoryRequest;
import com.echapps.ecom.project.category.dto.response.CategoryResponse;
import com.echapps.ecom.project.category.service.CategoryService;
import com.echapps.ecom.project.config.AppConstants;
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

@RestController()
@RequestMapping("/api/v1")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/public/categories")
    @Tag(name = "Category APIs", description = "APIs for managing categories")
    @Operation(summary = "Get all categories with pagination and sorting", description = "Retrieve a paginated and sorted list of all categories. You can specify the page number, page size, sorting field, and sorting order.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved categories"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<CategoryResponse> getAllCategories(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER_0, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE_50, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_BY_CATEGORY_ID, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.ASC_SORT_DIRECTION, required = false) String sortOrder) {
        CategoryResponse categoryResponse = categoryService.getAllCategories(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(categoryResponse, HttpStatus.OK);
    }

    @PostMapping("/public/categories")
    @Tag(name = "Category APIs", description = "APIs for managing categories")
    @Operation(summary = "Create a new category", description = "Create a new category by providing the category details in the request body.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid category data", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<CategoryRequest> createCategory(@Valid @RequestBody CategoryRequest categoryRequest) {
        CategoryRequest createdCategory = categoryService.createCategory(categoryRequest);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    @DeleteMapping("/admin/categories/{id}")
    @Tag(name = "Category APIs", description = "APIs for managing categories")
    @Operation(summary = "Delete a category by ID", description = "Delete a category by providing its ID in the path variable.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<CategoryRequest> deleteCategory(@Parameter(description = "ID of category to delete")
                                                          @PathVariable Long id) {
            CategoryRequest deletedCategory = categoryService.deleteCategory(id);
            return new ResponseEntity<>(deletedCategory, HttpStatus.OK);
    }

    @PutMapping("/public/categories/{categoryId}")
    @Tag(name = "Category APIs", description = "APIs for managing categories")
    @Operation(summary = "Update a category by ID", description = "Update a category by providing its ID in the path variable and the updated category details in the request body.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid category data", content = @Content),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<CategoryRequest> updateCategory(@Valid @RequestBody CategoryRequest categoryRequest,
                                                          @Parameter(description = "ID of category to update")
                                                          @PathVariable Long categoryId) {
        CategoryRequest updatedCategory = categoryService.updateCategory(categoryRequest, categoryId);
        return new ResponseEntity<>(updatedCategory, HttpStatus.OK);
    }

}

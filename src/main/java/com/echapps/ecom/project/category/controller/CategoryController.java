package com.echapps.ecom.project.category.controller;

import com.echapps.ecom.project.category.dto.request.CategoryRequest;
import com.echapps.ecom.project.category.dto.response.CategoryResponse;
import com.echapps.ecom.project.category.service.CategoryService;
import com.echapps.ecom.project.config.AppConstants;
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
    public ResponseEntity<CategoryResponse> getAllCategories(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER_0, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE_50, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_BY_CATEGORY_ID, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.ASC_SORT_DIRECTION, required = false) String sortOrder) {
        CategoryResponse categoryResponse = categoryService.getAllCategories(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(categoryResponse, HttpStatus.OK);
    }

    @PostMapping("/public/categories")
    public ResponseEntity<CategoryRequest> createCategory(@Valid @RequestBody CategoryRequest categoryRequest) {
        CategoryRequest createdCategory = categoryService.createCategory(categoryRequest);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    @DeleteMapping("/admin/categories/{id}")
    public ResponseEntity<CategoryRequest> deleteCategory(@PathVariable Long id) {
            CategoryRequest deletedCategory = categoryService.deleteCategory(id);
            return new ResponseEntity<>(deletedCategory, HttpStatus.OK);
    }

    @PutMapping("/public/categories/{categoryId}")
    public ResponseEntity<CategoryRequest> updateCategory(@Valid @RequestBody CategoryRequest categoryRequest,
                                                 @PathVariable Long categoryId) {
        CategoryRequest updatedCategory = categoryService.updateCategory(categoryRequest, categoryId);
        return new ResponseEntity<>(updatedCategory, HttpStatus.OK);
    }

}

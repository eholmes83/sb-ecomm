package com.echapps.ecom.project.category.service;

import com.echapps.ecom.project.category.dto.request.CategoryRequest;
import com.echapps.ecom.project.category.dto.response.CategoryResponse;

public interface CategoryService {

    CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    CategoryRequest createCategory(CategoryRequest categoryRequest);
    CategoryRequest deleteCategory(Long id);
    CategoryRequest updateCategory(CategoryRequest categoryRequest, Long categoryId);

}

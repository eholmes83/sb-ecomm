package com.echapps.ecom.project.category.service;

import com.echapps.ecom.project.category.model.Category;

import java.util.List;

public interface CategoryService {

    List<Category> getAllCategories();
    void createCategory(Category category);
    void deleteCategory(Long id);
    void updateCategory(Category category, Long categoryId);

}

package com.echapps.ecom.project.category.service;

import com.echapps.ecom.project.category.model.Category;

import java.util.List;

public interface CategoryService {

    List<Category> getAllCategories();
    void createCategory(Category category);
    String deleteCategory(Long id);
    Category updateCategory(Category category, Long categoryId);

}

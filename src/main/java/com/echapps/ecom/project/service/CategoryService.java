package com.echapps.ecom.project.service;

import com.echapps.ecom.project.model.Category;

import java.util.List;

public interface CategoryService {

    List<Category> getAllCategories();
    void createCategory(Category category);
    String deleteCategory(Long id);

}

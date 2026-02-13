package com.echapps.ecom.project.category.service;

import com.echapps.ecom.project.category.model.Category;
import com.echapps.ecom.project.category.repository.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public void createCategory(Category category) {
        categoryRepository.save(category);
    }

    @Override
    public void deleteCategory(Long id) {
       Category category = categoryRepository.findById(id)
               .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

       categoryRepository.delete(category);
    }

    @Override
    public Category updateCategory(Category category, Long categoryId) {
        Category updatedCategory = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        updatedCategory.setCategoryName(category.getCategoryName());
        return categoryRepository.save(updatedCategory);
    }

}

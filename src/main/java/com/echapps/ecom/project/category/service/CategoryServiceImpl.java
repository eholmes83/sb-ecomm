package com.echapps.ecom.project.category.service;

import com.echapps.ecom.project.category.model.Category;
import com.echapps.ecom.project.category.repository.CategoryRepository;
import com.echapps.ecom.project.exceptions.APIException;
import com.echapps.ecom.project.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty()) {
            throw new APIException("No categories found!");
        }
        return categories;
    }

    @Override
    public void createCategory(Category category) {
        Category savedCategory = categoryRepository.findByCategoryName(category.getCategoryName());
        if (savedCategory != null) {
            throw new APIException("Category with name " + category.getCategoryName() + " already exists!");
        }
        categoryRepository.save(category);
    }

    @Override
    public void deleteCategory(Long id) {
       Category category = categoryRepository.findById(id)
               .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", id));

       categoryRepository.delete(category);
    }

    @Override
    public void updateCategory(Category category, Long categoryId) {
        Category updatedCategory = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        updatedCategory.setCategoryName(category.getCategoryName());
        categoryRepository.save(updatedCategory);
    }

}

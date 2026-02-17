package com.echapps.ecom.project.category.service;

import com.echapps.ecom.project.category.dto.request.CategoryRequest;
import com.echapps.ecom.project.category.dto.response.CategoryResponse;
import com.echapps.ecom.project.category.model.Category;
import com.echapps.ecom.project.category.repository.CategoryRepository;
import com.echapps.ecom.project.exceptions.APIException;
import com.echapps.ecom.project.exceptions.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final ObjectMapper mapper;

    public CategoryServiceImpl(CategoryRepository categoryRepository, ObjectMapper mapper) {
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
    }

    @Override
    public CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Category> categoryPage = categoryRepository.findAll(pageDetails);
        List<Category> categories = categoryPage.getContent();

        if (categories.isEmpty()) {
            throw new APIException("No categories found!");
        }

        List<CategoryRequest> categoryRequests = categories.stream()
                .map(category -> mapper.convertValue(category, CategoryRequest.class))
                .toList();

        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setContent(categoryRequests);
        categoryResponse.setPageNumber(categoryPage.getNumber());
        categoryResponse.setPageSize(categoryPage.getSize());
        categoryResponse.setTotalElements(categoryPage.getTotalElements());
        categoryResponse.setTotalPages(categoryPage.getTotalPages());
        categoryResponse.setLastPage(categoryPage.isLast());
        return categoryResponse;
    }

    @Override
    public CategoryRequest createCategory(CategoryRequest categoryRequest) {
        Category category = mapper.convertValue(categoryRequest, Category.class);
        Category savedCategory = categoryRepository.findByCategoryName(category.getCategoryName());
        if (savedCategory != null) {
            throw new APIException("Category with name " + categoryRequest.getCategoryName() + " already exists!");
        }
        Category newCategory = categoryRepository.save(category);
        return mapper.convertValue(newCategory, CategoryRequest.class);
    }

    @Override
    public CategoryRequest deleteCategory(Long id) {
       Category category = categoryRepository.findById(id)
               .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", id));

       categoryRepository.delete(category);
       return mapper.convertValue(category, CategoryRequest.class);
    }

    @Override
    public CategoryRequest updateCategory(CategoryRequest categoryRequest, Long categoryId) {
        Category updatedCategory = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        Category category = mapper.convertValue(categoryRequest, Category.class);
        category.setCategoryId(categoryId);
        categoryRepository.save(category);
        return mapper.convertValue(updatedCategory, CategoryRequest.class);
    }

}

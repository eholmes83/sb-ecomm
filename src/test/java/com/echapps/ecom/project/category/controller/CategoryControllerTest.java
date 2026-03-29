package com.echapps.ecom.project.category.controller;

import com.echapps.ecom.project.category.dto.request.CategoryRequest;
import com.echapps.ecom.project.category.dto.response.CategoryResponse;
import com.echapps.ecom.project.category.service.CategoryService;
import com.echapps.ecom.project.config.AppConstants;
import com.echapps.ecom.project.exceptions.APIException;
import com.echapps.ecom.project.exceptions.GlobalExceptionHandler;
import com.echapps.ecom.project.exceptions.ResourceNotFoundException;
import com.echapps.ecom.project.security.jwt.AuthTokenFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = CategoryController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthTokenFilter.class)
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void getAllCategoriesShouldReturnPagedCategoriesUsingDefaultRequestParameters() throws Exception {
        CategoryRequest electronics = new CategoryRequest(1L, "Electronics");
        CategoryRequest books = new CategoryRequest(2L, "Books");
        CategoryResponse categoryResponse = new CategoryResponse(List.of(electronics, books), 0, 50, 2L, 1, true);

        when(categoryService.getAllCategories(
                Integer.valueOf(AppConstants.PAGE_NUMBER_0),
                Integer.valueOf(AppConstants.PAGE_SIZE_50),
                AppConstants.SORT_BY_CATEGORY_ID,
                AppConstants.ASC_SORT_DIRECTION))
                .thenReturn(categoryResponse);

        mockMvc.perform(get("/api/v1/public/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].categoryId").value(1))
                .andExpect(jsonPath("$.content[0].categoryName").value("Electronics"))
                .andExpect(jsonPath("$.content[1].categoryId").value(2))
                .andExpect(jsonPath("$.content[1].categoryName").value("Books"))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(50))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.lastPage").value(true));

        verify(categoryService).getAllCategories(0, 50, "categoryId", "asc");
    }

    @Test
    void getAllCategoriesShouldForwardExplicitPaginationAndSortingParameters() throws Exception {
        CategoryRequest accessories = new CategoryRequest(11L, "Accessories");
        CategoryResponse categoryResponse = new CategoryResponse(List.of(accessories), 2, 1, 3L, 3, false);

        when(categoryService.getAllCategories(2, 1, "categoryName", "desc"))
                .thenReturn(categoryResponse);

        mockMvc.perform(get("/api/v1/public/categories")
                        .param("pageNumber", "2")
                        .param("pageSize", "1")
                        .param("sortBy", "categoryName")
                        .param("sortOrder", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].categoryId").value(11))
                .andExpect(jsonPath("$.content[0].categoryName").value("Accessories"))
                .andExpect(jsonPath("$.pageNumber").value(2))
                .andExpect(jsonPath("$.pageSize").value(1))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.lastPage").value(false));

        verify(categoryService).getAllCategories(2, 1, "categoryName", "desc");
    }

    @Test
    void getAllCategoriesShouldReturnBadRequestWhenServiceThrowsApiException() throws Exception {
        when(categoryService.getAllCategories(0, 50, "categoryId", "asc"))
                .thenThrow(new APIException("Invalid pagination request"));

        mockMvc.perform(get("/api/v1/public/categories"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid pagination request"));

        verify(categoryService).getAllCategories(0, 50, "categoryId", "asc");
    }

    @Test
    void getAllCategoriesShouldReturnBadRequestWhenPageNumberIsNotNumeric() throws Exception {
        mockMvc.perform(get("/api/v1/public/categories")
                        .param("pageNumber", "abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(categoryService);
    }

    @Test
    void createCategoryShouldReturnCreatedCategory() throws Exception {
        CategoryRequest categoryRequest = new CategoryRequest(null, "Garden");
        CategoryRequest createdCategory = new CategoryRequest(7L, "Garden");

        when(categoryService.createCategory(categoryRequest)).thenReturn(createdCategory);

        mockMvc.perform(post("/api/v1/public/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryId").value(7))
                .andExpect(jsonPath("$.categoryName").value("Garden"));

        verify(categoryService).createCategory(categoryRequest);
    }

    @Test
    void createCategoryShouldAllowEmptyPayloadFieldsBecauseDtoHasNoValidationConstraints() throws Exception {
        CategoryRequest categoryRequest = new CategoryRequest(null, null);
        CategoryRequest createdCategory = new CategoryRequest(15L, null);

        when(categoryService.createCategory(categoryRequest)).thenReturn(createdCategory);

        mockMvc.perform(post("/api/v1/public/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryId").value(15))
                .andExpect(jsonPath("$.categoryName").value(Matchers.nullValue()));

        verify(categoryService).createCategory(categoryRequest);
    }

    @Test
    void createCategoryShouldReturnBadRequestWhenServiceThrowsApiException() throws Exception {
        CategoryRequest categoryRequest = new CategoryRequest(null, "Electronics");

        when(categoryService.createCategory(categoryRequest))
                .thenThrow(new APIException("Category with name Electronics already exists"));

        mockMvc.perform(post("/api/v1/public/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Category with name Electronics already exists"));

        verify(categoryService).createCategory(categoryRequest);
    }

    @Test
    void createCategoryShouldReturnBadRequestForMalformedJson() throws Exception {
        mockMvc.perform(post("/api/v1/public/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryName\":\"Electronics\""))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(categoryService);
    }

    @Test
    void deleteCategoryShouldReturnDeletedCategory() throws Exception {
        CategoryRequest deletedCategory = new CategoryRequest(3L, "Fashion");

        when(categoryService.deleteCategory(3L)).thenReturn(deletedCategory);

        mockMvc.perform(delete("/api/v1/admin/categories/{id}", 3L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(3))
                .andExpect(jsonPath("$.categoryName").value("Fashion"));

        verify(categoryService).deleteCategory(3L);
    }

    @Test
    void deleteCategoryShouldReturnNotFoundWhenServiceThrowsResourceNotFoundException() throws Exception {
        when(categoryService.deleteCategory(99L))
                .thenThrow(new ResourceNotFoundException("Category", "categoryId", 99L));

        mockMvc.perform(delete("/api/v1/admin/categories/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Category not found with categoryId : 99"));

        verify(categoryService).deleteCategory(99L);
    }

    @Test
    void deleteCategoryShouldReturnBadRequestWhenIdIsNotNumeric() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/categories/{id}", "bad-id"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(categoryService);
    }

    @Test
    void updateCategoryShouldReturnUpdatedCategory() throws Exception {
        CategoryRequest categoryRequest = new CategoryRequest(4L, "Home Decor");
        CategoryRequest updatedCategory = new CategoryRequest(4L, "Home Decor");

        when(categoryService.updateCategory(categoryRequest, 4L)).thenReturn(updatedCategory);

        mockMvc.perform(put("/api/v1/public/categories/{categoryId}", 4L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(4))
                .andExpect(jsonPath("$.categoryName").value("Home Decor"));

        verify(categoryService).updateCategory(categoryRequest, 4L);
    }

    @Test
    void updateCategoryShouldReturnNotFoundWhenServiceThrowsResourceNotFoundException() throws Exception {
        CategoryRequest categoryRequest = new CategoryRequest(101L, "Legacy");

        when(categoryService.updateCategory(categoryRequest, 101L))
                .thenThrow(new ResourceNotFoundException("Category", "categoryId", 101L));

        mockMvc.perform(put("/api/v1/public/categories/{categoryId}", 101L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Category not found with categoryId : 101"));

        verify(categoryService).updateCategory(categoryRequest, 101L);
    }

    @Test
    void updateCategoryShouldReturnBadRequestWhenServiceThrowsApiException() throws Exception {
        CategoryRequest categoryRequest = new CategoryRequest(4L, "");

        when(categoryService.updateCategory(categoryRequest, 4L))
                .thenThrow(new APIException("Category name cannot be blank"));

        mockMvc.perform(put("/api/v1/public/categories/{categoryId}", 4L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Category name cannot be blank"));

        verify(categoryService).updateCategory(categoryRequest, 4L);
    }

    @Test
    void updateCategoryShouldReturnBadRequestWhenCategoryIdPathVariableIsNotNumeric() throws Exception {
        mockMvc.perform(put("/api/v1/public/categories/{categoryId}", "bad-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryRequest(1L, "Office"))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(categoryService);
    }
}

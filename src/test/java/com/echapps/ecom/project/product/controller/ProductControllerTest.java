package com.echapps.ecom.project.product.controller;

import com.echapps.ecom.project.config.AppConstants;
import com.echapps.ecom.project.exceptions.APIException;
import com.echapps.ecom.project.exceptions.GlobalExceptionHandler;
import com.echapps.ecom.project.exceptions.ResourceNotFoundException;
import com.echapps.ecom.project.product.dto.request.ProductRequest;
import com.echapps.ecom.project.product.dto.response.ProductResponse;
import com.echapps.ecom.project.product.service.ProductService;
import com.echapps.ecom.project.security.jwt.AuthTokenFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ProductController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthTokenFilter.class)
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ProductService productService;

    // =========================================================================
    // addProduct — POST /api/v1/admin/categories/{categoryId}/product
    // =========================================================================

    @Nested
    @DisplayName("addProduct — POST /api/v1/admin/categories/{categoryId}/product")
    class AddProduct {

        @Test
        void addProductShouldReturnCreatedProductWithGeneratedId() throws Exception {
            ProductRequest input   = new ProductRequest(null, "Phone", null, "Flagship phone", 10, 900.0, 10.0, 810.0);
            ProductRequest created = new ProductRequest(5L,   "Phone", null, "Flagship phone", 10, 900.0, 10.0, 810.0);

            when(productService.addProduct(input, 2L)).thenReturn(created);

            mockMvc.perform(post("/api/v1/admin/categories/{categoryId}/product", 2L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.productId").value(5))
                    .andExpect(jsonPath("$.productName").value("Phone"))
                    .andExpect(jsonPath("$.price").value(900.0))
                    .andExpect(jsonPath("$.specialPrice").value(810.0));

            verify(productService).addProduct(input, 2L);
        }

        @Test
        void addProductShouldReturnNotFoundWhenCategoryDoesNotExist() throws Exception {
            ProductRequest input = new ProductRequest(null, "Phone", null, "Flagship phone", 10, 900.0, 10.0, 810.0);

            when(productService.addProduct(input, 77L))
                    .thenThrow(new ResourceNotFoundException("Category", "categoryId", 77L));

            mockMvc.perform(post("/api/v1/admin/categories/{categoryId}/product", 77L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Category not found with categoryId : 77"));

            verify(productService).addProduct(input, 77L);
        }

        @Test
        void addProductShouldReturnBadRequestWhenServiceThrowsApiException() throws Exception {
            ProductRequest input = new ProductRequest(null, "Phone", null, "Flagship phone", 10, 900.0, 10.0, 810.0);

            when(productService.addProduct(input, 2L))
                    .thenThrow(new APIException("Product Phone already exists in this category"));

            mockMvc.perform(post("/api/v1/admin/categories/{categoryId}/product", 2L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Product Phone already exists in this category"));

            verify(productService).addProduct(input, 2L);
        }

        @Test
        void addProductShouldReturnBadRequestWhenJsonIsMalformed() throws Exception {
            mockMvc.perform(post("/api/v1/admin/categories/{categoryId}/product", 2L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"productName\":\"Phone\""))   // missing closing brace
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(productService);
        }

        @Test
        void addProductShouldReturnBadRequestWhenCategoryIdIsNotNumeric() throws Exception {
            mockMvc.perform(post("/api/v1/admin/categories/{categoryId}/product", "bad-id")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new ProductRequest(null, "Phone", null, "", 1, 1.0, 0.0, 1.0))))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(productService);
        }
    }

    // =========================================================================
    // getAllProducts — GET /api/v1/public/products
    // =========================================================================

    @Nested
    @DisplayName("getAllProducts — GET /api/v1/public/products")
    class GetAllProducts {

        @Test
        void getAllProductsShouldReturnPagedProductsUsingDefaultRequestParameters() throws Exception {
            ProductRequest p1 = new ProductRequest(1L, "Phone",  null, "", 5, 999.0, 0.0, 999.0);
            ProductRequest p2 = new ProductRequest(2L, "Tablet", null, "", 3, 499.0, 5.0, 474.0);
            ProductResponse response = new ProductResponse(List.of(p1, p2), 0, 50, 2L, 1, true);

            when(productService.getAllProducts(
                    Integer.valueOf(AppConstants.PAGE_NUMBER_0),
                    Integer.valueOf(AppConstants.PAGE_SIZE_50),
                    AppConstants.SORT_BY_PRODUCT_ID,
                    AppConstants.ASC_SORT_DIRECTION))
                    .thenReturn(response);

            mockMvc.perform(get("/api/v1/public/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].productId").value(1))
                    .andExpect(jsonPath("$.content[0].productName").value("Phone"))
                    .andExpect(jsonPath("$.content[1].productId").value(2))
                    .andExpect(jsonPath("$.content[1].productName").value("Tablet"))
                    .andExpect(jsonPath("$.pageNumber").value(0))
                    .andExpect(jsonPath("$.pageSize").value(50))
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.lastPage").value(true));

            verify(productService).getAllProducts(0, 50, "productId", "asc");
        }

        @Test
        void getAllProductsShouldForwardExplicitPaginationAndSortingParameters() throws Exception {
            ProductRequest p1 = new ProductRequest(2L, "Watch", null, "", 1, 120.0, 0.0, 120.0);
            ProductResponse response = new ProductResponse(List.of(p1), 1, 1, 2L, 2, false);

            when(productService.getAllProducts(1, 1, "productName", "desc")).thenReturn(response);

            mockMvc.perform(get("/api/v1/public/products")
                            .param("pageNumber", "1")
                            .param("pageSize",   "1")
                            .param("sortBy",     "productName")
                            .param("sortOrder",  "desc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].productName").value("Watch"))
                    .andExpect(jsonPath("$.pageNumber").value(1))
                    .andExpect(jsonPath("$.pageSize").value(1))
                    .andExpect(jsonPath("$.lastPage").value(false));

            verify(productService).getAllProducts(1, 1, "productName", "desc");
        }

        @Test
        void getAllProductsShouldReturnBadRequestWhenServiceThrowsApiException() throws Exception {
            when(productService.getAllProducts(0, 50, "productId", "asc"))
                    .thenThrow(new APIException("No products found!"));

            mockMvc.perform(get("/api/v1/public/products"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("No products found!"));

            verify(productService).getAllProducts(0, 50, "productId", "asc");
        }

        @Test
        void getAllProductsShouldReturnBadRequestWhenPageNumberIsNotNumeric() throws Exception {
            mockMvc.perform(get("/api/v1/public/products").param("pageNumber", "abc"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(productService);
        }

        @Test
        void getAllProductsShouldReturnBadRequestWhenPageSizeIsNotNumeric() throws Exception {
            mockMvc.perform(get("/api/v1/public/products").param("pageSize", "abc"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(productService);
        }
    }

    // =========================================================================
    // getProductsByCategory — GET /api/v1/public/categories/{categoryId}/products
    // =========================================================================

    @Nested
    @DisplayName("getProductsByCategory — GET /api/v1/public/categories/{categoryId}/products")
    class GetProductsByCategory {

        @Test
        void getProductsByCategoryShouldReturnPagedProductsUsingDefaultRequestParameters() throws Exception {
            ProductRequest p1 = new ProductRequest(10L, "Laptop", null, "", 1, 1000.0, 0.0, 1000.0);
            ProductResponse response = new ProductResponse(List.of(p1), 0, 50, 1L, 1, true);

            when(productService.searchByCategory(3L, 0, 50, "productId", "asc")).thenReturn(response);

            mockMvc.perform(get("/api/v1/public/categories/{categoryId}/products", 3L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].productId").value(10))
                    .andExpect(jsonPath("$.content[0].productName").value("Laptop"))
                    .andExpect(jsonPath("$.pageNumber").value(0))
                    .andExpect(jsonPath("$.totalElements").value(1));

            verify(productService).searchByCategory(3L, 0, 50, "productId", "asc");
        }

        @Test
        void getProductsByCategoryShouldForwardExplicitPaginationAndSortingParameters() throws Exception {
            ProductRequest p1 = new ProductRequest(10L, "Laptop", null, "", 1, 1000.0, 0.0, 1000.0);
            ProductResponse response = new ProductResponse(List.of(p1), 2, 1, 5L, 5, false);

            when(productService.searchByCategory(3L, 2, 1, "productName", "desc")).thenReturn(response);

            mockMvc.perform(get("/api/v1/public/categories/{categoryId}/products", 3L)
                            .param("pageNumber", "2")
                            .param("pageSize",   "1")
                            .param("sortBy",     "productName")
                            .param("sortOrder",  "desc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pageNumber").value(2))
                    .andExpect(jsonPath("$.lastPage").value(false));

            verify(productService).searchByCategory(3L, 2, 1, "productName", "desc");
        }

        @Test
        void getProductsByCategoryShouldReturnNotFoundWhenCategoryDoesNotExist() throws Exception {
            when(productService.searchByCategory(99L, 0, 50, "productId", "asc"))
                    .thenThrow(new ResourceNotFoundException("Category", "categoryId", 99L));

            mockMvc.perform(get("/api/v1/public/categories/{categoryId}/products", 99L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Category not found with categoryId : 99"));

            verify(productService).searchByCategory(99L, 0, 50, "productId", "asc");
        }

        @Test
        void getProductsByCategoryShouldReturnBadRequestWhenServiceThrowsApiException() throws Exception {
            when(productService.searchByCategory(3L, 0, 50, "productId", "asc"))
                    .thenThrow(new APIException("No products found in this category"));

            mockMvc.perform(get("/api/v1/public/categories/{categoryId}/products", 3L))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("No products found in this category"));

            verify(productService).searchByCategory(3L, 0, 50, "productId", "asc");
        }

        @Test
        void getProductsByCategoryShouldReturnBadRequestWhenCategoryIdIsNotNumeric() throws Exception {
            mockMvc.perform(get("/api/v1/public/categories/{categoryId}/products", "bad-id"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(productService);
        }
    }

    // =========================================================================
    // searchProductsByKeyword — GET /api/v1/public/products/keyword/{keyword}
    // Note: controller explicitly returns HttpStatus.FOUND (302)
    // =========================================================================

    @Nested
    @DisplayName("searchProductsByKeyword — GET /api/v1/public/products/keyword/{keyword}")
    class SearchProductsByKeyword {

        @Test
        void searchProductsByKeywordShouldReturnFoundStatusWithMatchingProducts() throws Exception {
            ProductRequest p1 = new ProductRequest(8L, "Gaming Mouse", null, "", 4, 49.0, 0.0, 49.0);
            ProductResponse response = new ProductResponse(List.of(p1), 0, 50, 1L, 1, true);

            when(productService.searchProductsByKeyword("mouse", 0, 50, "productId", "asc"))
                    .thenReturn(response);

            mockMvc.perform(get("/api/v1/public/products/keyword/{keyword}", "mouse"))
                    .andExpect(status().isFound())
                    .andExpect(jsonPath("$.content[0].productId").value(8))
                    .andExpect(jsonPath("$.content[0].productName").value("Gaming Mouse"))
                    .andExpect(jsonPath("$.totalElements").value(1));

            verify(productService).searchProductsByKeyword("mouse", 0, 50, "productId", "asc");
        }

        @Test
        void searchProductsByKeywordShouldForwardExplicitPaginationAndSortingParameters() throws Exception {
            ProductRequest p1 = new ProductRequest(8L, "Gaming Mouse", null, "", 4, 49.0, 0.0, 49.0);
            ProductResponse response = new ProductResponse(List.of(p1), 1, 1, 3L, 3, false);

            when(productService.searchProductsByKeyword("mouse", 1, 1, "productName", "desc"))
                    .thenReturn(response);

            mockMvc.perform(get("/api/v1/public/products/keyword/{keyword}", "mouse")
                            .param("pageNumber", "1")
                            .param("pageSize",   "1")
                            .param("sortBy",     "productName")
                            .param("sortOrder",  "desc"))
                    .andExpect(status().isFound())
                    .andExpect(jsonPath("$.pageNumber").value(1))
                    .andExpect(jsonPath("$.lastPage").value(false));

            verify(productService).searchProductsByKeyword("mouse", 1, 1, "productName", "desc");
        }

        @Test
        void searchProductsByKeywordShouldReturnBadRequestWhenServiceThrowsApiException() throws Exception {
            when(productService.searchProductsByKeyword("xyz", 0, 50, "productId", "asc"))
                    .thenThrow(new APIException("No products found matching keyword xyz"));

            mockMvc.perform(get("/api/v1/public/products/keyword/{keyword}", "xyz"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("No products found matching keyword xyz"));

            verify(productService).searchProductsByKeyword("xyz", 0, 50, "productId", "asc");
        }
    }

    // =========================================================================
    // updateProduct — PUT /api/v1/admin/products/{productId}
    // =========================================================================

    @Nested
    @DisplayName("updateProduct — PUT /api/v1/admin/products/{productId}")
    class UpdateProduct {

        @Test
        void updateProductShouldReturnUpdatedProduct() throws Exception {
            ProductRequest input   = new ProductRequest(4L, "Keyboard", null, "Mechanical keyboard", 5, 89.0, 0.0, 89.0);
            ProductRequest updated = new ProductRequest(4L, "Keyboard", null, "Mechanical keyboard", 5, 89.0, 0.0, 89.0);

            when(productService.updateProduct(input, 4L)).thenReturn(updated);

            mockMvc.perform(put("/api/v1/admin/products/{productId}", 4L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.productId").value(4))
                    .andExpect(jsonPath("$.productName").value("Keyboard"))
                    .andExpect(jsonPath("$.price").value(89.0));

            verify(productService).updateProduct(input, 4L);
        }

        @Test
        void updateProductShouldReturnNotFoundWhenProductDoesNotExist() throws Exception {
            ProductRequest input = new ProductRequest(101L, "Legacy", null, "", 1, 10.0, 0.0, 10.0);

            when(productService.updateProduct(input, 101L))
                    .thenThrow(new ResourceNotFoundException("Product", "productId", 101L));

            mockMvc.perform(put("/api/v1/admin/products/{productId}", 101L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Product not found with productId : 101"));

            verify(productService).updateProduct(input, 101L);
        }

        @Test
        void updateProductShouldReturnBadRequestWhenServiceThrowsApiException() throws Exception {
            ProductRequest input = new ProductRequest(4L, "Keyboard", null, "", 1, 89.0, 0.0, 89.0);

            when(productService.updateProduct(input, 4L))
                    .thenThrow(new APIException("Product name already taken"));

            mockMvc.perform(put("/api/v1/admin/products/{productId}", 4L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Product name already taken"));

            verify(productService).updateProduct(input, 4L);
        }

        @Test
        void updateProductShouldReturnBadRequestWhenJsonIsMalformed() throws Exception {
            mockMvc.perform(put("/api/v1/admin/products/{productId}", 4L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"productName\":\"Keyboard\""))   // missing closing brace
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(productService);
        }

        @Test
        void updateProductShouldReturnBadRequestWhenProductIdIsNotNumeric() throws Exception {
            mockMvc.perform(put("/api/v1/admin/products/{productId}", "bad-id")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new ProductRequest(1L, "Keyboard", null, "", 1, 1.0, 0.0, 1.0))))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(productService);
        }
    }

    // =========================================================================
    // deleteProduct — DELETE /api/v1/admin/products/{productId}
    // =========================================================================

    @Nested
    @DisplayName("deleteProduct — DELETE /api/v1/admin/products/{productId}")
    class DeleteProduct {

        @Test
        void deleteProductShouldReturnDeletedProduct() throws Exception {
            ProductRequest deleted = new ProductRequest(9L, "Headphones", null, "", 1, 59.0, 0.0, 59.0);

            when(productService.deleteProduct(9L)).thenReturn(deleted);

            mockMvc.perform(delete("/api/v1/admin/products/{productId}", 9L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.productId").value(9))
                    .andExpect(jsonPath("$.productName").value("Headphones"));

            verify(productService).deleteProduct(9L);
        }

        @Test
        void deleteProductShouldReturnNotFoundWhenProductDoesNotExist() throws Exception {
            when(productService.deleteProduct(404L))
                    .thenThrow(new ResourceNotFoundException("Product", "productId", 404L));

            mockMvc.perform(delete("/api/v1/admin/products/{productId}", 404L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Product not found with productId : 404"));

            verify(productService).deleteProduct(404L);
        }

        @Test
        void deleteProductShouldReturnBadRequestWhenProductIdIsNotNumeric() throws Exception {
            mockMvc.perform(delete("/api/v1/admin/products/{productId}", "bad-id"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(productService);
        }
    }

    // =========================================================================
    // updateProductImage — PUT /api/v1/admin/products/{productId}/image
    // =========================================================================

    @Nested
    @DisplayName("updateProductImage — PUT /api/v1/admin/products/{productId}/image")
    class UpdateProductImage {

        @Test
        void updateProductImageShouldReturnUpdatedProductWithNewImageUrl() throws Exception {
            MockMultipartFile image = new MockMultipartFile("image", "phone.jpg", "image/jpeg", "img-bytes".getBytes());
            ProductRequest updated  = new ProductRequest(12L, "Phone", "images/phone.jpg", "", 1, 900.0, 0.0, 900.0);

            when(productService.updateProductImage(12L, image)).thenReturn(updated);

            MockMultipartHttpServletRequestBuilder request =
                    multipart("/api/v1/admin/products/{productId}/image", 12L).file(image);
            request.with(req -> { req.setMethod("PUT"); return req; });

            mockMvc.perform(request)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.productId").value(12))
                    .andExpect(jsonPath("$.image").value("images/phone.jpg"));

            verify(productService).updateProductImage(12L, image);
        }

        @Test
        void updateProductImageShouldReturnNotFoundWhenProductDoesNotExist() throws Exception {
            MockMultipartFile image = new MockMultipartFile("image", "phone.jpg", "image/jpeg", "img-bytes".getBytes());

            when(productService.updateProductImage(98L, image))
                    .thenThrow(new ResourceNotFoundException("Product", "productId", 98L));

            MockMultipartHttpServletRequestBuilder request =
                    multipart("/api/v1/admin/products/{productId}/image", 98L).file(image);
            request.with(req -> { req.setMethod("PUT"); return req; });

            mockMvc.perform(request)
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Product not found with productId : 98"));

            verify(productService).updateProductImage(98L, image);
        }

        @Test
        void updateProductImageShouldPropagateIOExceptionBecauseNoHandlerIsRegistered() throws Exception {
            // GlobalExceptionHandler does not handle IOException, so it propagates as a
            // NestedServletException wrapping the original IOException.
            MockMultipartFile image = new MockMultipartFile("image", "broken.jpg", "image/jpeg", "bad".getBytes());

            doThrow(new IOException("Disk write failed")).when(productService).updateProductImage(66L, image);

            MockMultipartHttpServletRequestBuilder request =
                    multipart("/api/v1/admin/products/{productId}/image", 66L).file(image);
            request.with(req -> { req.setMethod("PUT"); return req; });

            Exception thrown = assertThrows(Exception.class, () -> mockMvc.perform(request));
            Throwable root = thrown.getCause() != null ? thrown.getCause() : thrown;
            assertInstanceOf(IOException.class, root);

            verify(productService).updateProductImage(66L, image);
        }

        @Test
        void updateProductImageShouldReturnBadRequestWhenImagePartIsMissing() throws Exception {
            MockMultipartHttpServletRequestBuilder request =
                    multipart("/api/v1/admin/products/{productId}/image", 12L);
            request.with(req -> { req.setMethod("PUT"); return req; });

            mockMvc.perform(request)
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(productService);
        }

        @Test
        void updateProductImageShouldReturnBadRequestWhenProductIdIsNotNumeric() throws Exception {
            MockMultipartFile image = new MockMultipartFile("image", "phone.jpg", "image/jpeg", "img-bytes".getBytes());

            MockMultipartHttpServletRequestBuilder request =
                    multipart("/api/v1/admin/products/{productId}/image", "bad-id").file(image);
            request.with(req -> { req.setMethod("PUT"); return req; });

            mockMvc.perform(request)
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(productService);
        }
    }
}

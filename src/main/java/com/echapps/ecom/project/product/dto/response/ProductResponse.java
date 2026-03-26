package com.echapps.ecom.project.product.dto.response;

import com.echapps.ecom.project.product.dto.request.ProductRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    @Schema(description = "List of products in the current page")
    private List<ProductRequest> content;

    @Schema(description = "Current page number (0-indexed)", example = "0")
    private Integer pageNumber;

    @Schema(description = "Number of products per page", example = "10")
    private Integer pageSize;

    @Schema(description = "Total number of products across all pages", example = "100")
    private Long totalElements;

    @Schema(description = "Total number of pages available", example = "10")
    private Integer totalPages;

    @Schema(description = "Indicates if this is the last page of results", example = "true")
    private Boolean lastPage;

}

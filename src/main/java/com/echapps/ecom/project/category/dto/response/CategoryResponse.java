package com.echapps.ecom.project.category.dto.response;

import com.echapps.ecom.project.category.dto.request.CategoryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponse {

    @Schema(description = "List of categories in the current page")
    private List<CategoryRequest> content;

    @Schema(description = "Current page number (0-indexed)", example = "0")
    private Integer pageNumber;

    @Schema(description = "Number of categories per page", example = "10")
    private Integer pageSize;

    @Schema(description = "Total number of categories across all pages", example = "100")
    private Long totalElements;

    @Schema(description = "Total number of pages available", example = "10")
    private Integer totalPages;

    @Schema(description = "Indicates if this is the last page of results", example = "true")
    private Boolean lastPage;

}

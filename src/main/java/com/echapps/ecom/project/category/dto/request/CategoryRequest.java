package com.echapps.ecom.project.category.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryRequest {
    @Schema(description = "Unique identifier for the category", example = "1")
    private Long categoryId;

    @Schema(description = "Name of the category", example = "Electronics")
    private String categoryName;
}

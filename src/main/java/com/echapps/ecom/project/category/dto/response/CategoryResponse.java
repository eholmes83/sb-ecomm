package com.echapps.ecom.project.category.dto.response;

import com.echapps.ecom.project.category.dto.request.CategoryRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponse {
    private List<CategoryRequest> content;

}

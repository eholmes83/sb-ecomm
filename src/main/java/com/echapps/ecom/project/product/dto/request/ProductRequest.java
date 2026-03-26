package com.echapps.ecom.project.product.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {

    @Schema(description = "Unique identifier for the product", example = "1")
    private Long productId;

    @Schema(description = "Name of the product", example = "Smartphone")
    private String productName;

    @Schema(description = "URL of the product image", example = "http://example.com/image.jpg")
    private String image;

    @Schema(description = "Detailed description of the product", example = "A high-end smartphone with 128GB storage")
    private String description;

    @Schema(description = "Available quantity of the product in stock", example = "100")
    private Integer quantity;

    @Schema(description = "Price of the product", example = "499.99")
    private Double price;

    @Schema(description = "Discount percentage for the product", example = "10.0")
    private Double discount;

    @Schema(description = "Special price of the product after applying discount", example = "449.99")
    private Double specialPrice;

    // Ignored to prevent circular reference serialization with Category entity
    //@JsonIgnore
    //@Schema(description = "Category to which the product belongs")
    //private Object category;
}

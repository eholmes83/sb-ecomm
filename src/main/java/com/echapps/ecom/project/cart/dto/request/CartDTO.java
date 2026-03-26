package com.echapps.ecom.project.cart.dto.request;

import com.echapps.ecom.project.product.dto.request.ProductRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartDTO {

    @Schema(description = "Unique identifier for the cart", example = "1")
    private Long cartId;

    @Schema(description = "Total price of all products in the cart", example = "999.99")
    private Double totalPrice = 0.0;

    @Schema(description = "List of products in the cart")
    private List<ProductRequest> products = new ArrayList<>();
}

package com.echapps.ecom.project.cart.dto.request;

import com.echapps.ecom.project.product.dto.request.ProductRequest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDTO {
    private Long cartItemId;
    @JsonIgnore
    private CartDTO cart;
    private ProductRequest product;
    private Integer quantity;
    private Double discount;
    private Double productPrice;
}
